/**
 * 
 */
package org.snowjak.hivemind.engine.prefab

import java.util.logging.Logger

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.snowjak.hivemind.Context
import org.snowjak.hivemind.Factions
import org.snowjak.hivemind.RNG
import org.snowjak.hivemind.Factions.Faction
import org.snowjak.hivemind.engine.Tags
import org.snowjak.hivemind.engine.components.HasMap
import org.snowjak.hivemind.engine.components.IsFromPrefab
import org.snowjak.hivemind.engine.systems.manager.EntityRefManager
import org.snowjak.hivemind.engine.systems.manager.FactionManager
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager
import org.snowjak.hivemind.util.ExtGreasedRegion

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ai.btree.Task.Status
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap

import io.github.classgraph.ClassGraph
import squidpony.squidgrid.gui.gdx.SColor
import squidpony.squidmath.SquidID

/**
 * @author snowjak88
 *
 */
public abstract class PrefabScript extends Script {
	
	private static final Logger LOG = Logger.getLogger(PrefabScript.class.name)
	
	private static final String ROOT_PATH = "data${File.separator}prefab${File.separator}"
	private static final BiMap<String,PrefabScript> CACHED_SCRIPTS = HashBiMap.create();
	private static CompilerConfiguration COMPILER_CONFIG = null;
	
	/**
	 * Load the given PrefabScript by name -- e.g.:
	 * <pre>
	 *   PrefabScript.byName("my-entity")
	 *   
	 * will evaluate to
	 *   
	 *   .../data/prefab/my-entity.groovy
	 * </pre>
	 * @param name
	 * @return {@code null} if the given prefab cannot be loaded for any reason
	 */
	public static PrefabScript byName(String name) {
		
		if(!CACHED_SCRIPTS.containsKey(name)) {
			
			def f = Gdx.files.local(ROOT_PATH + name + ".groovy").file;
			if(!f.exists()) {
				LOG.severe "Cannot load prefab-script [$name] -- does not exist at $f.path"
				return null
			}
			if(!f.isFile()) {
				LOG.severe "Cannot load prefab-script [$name] -- $f.path is not a file"
				return null
			}
			
			if(COMPILER_CONFIG == null) {
				def icz = new ImportCustomizer()
				icz.addImport "Color", SColor.class.name
				icz.addImport "Region", ExtGreasedRegion.class.name
				icz.addImport "RNG", RNG.class.name
				icz.addImport "Status", Status.class.name
				icz.addImport "Tags", Tags.class.name
				def sr = new ClassGraph().enableClassInfo().whitelistPackages("org.snowjak.hivemind").scan()
				sr.getClassesImplementing(Component.class.name).filter({!it.isAbstract() && !it.isInterfaceOrAnnotation()}).forEach {
					icz.addImports(it.loadClass().name)
				}
				
				
				COMPILER_CONFIG = new CompilerConfiguration()
				COMPILER_CONFIG.scriptBaseClass = PrefabScript.class.name
				COMPILER_CONFIG.compilationCustomizers << icz
			}
			
			def shell = new GroovyShell(this.class.classLoader, new Binding(), COMPILER_CONFIG)
			
			CACHED_SCRIPTS[name] = shell.parse(f)
		}
		
		CACHED_SCRIPTS[name];
	}
	
	/**
	 * Execute this PrefabScript. Creates a new {@link Entity} using the current
	 * {@link org.snowjak.hivemind.engine.Engine}. Takes care of adding that Entity to the Engine.
	 * 
	 * @throws RuntimeException if the prefab-script cannot be executed
	 */
	@Override
	public Entity run() {
		
		def name = CACHED_SCRIPTS.inverse()[this]
		
		binding.variables["entity"] = Context.getEngine().createEntity()
		
		scriptBody()
		
		def label = binding.variables["label"]
		def prefab = binding.variables["prefab"]
		
		if(prefab == null)
			throw new RuntimeException("Cannot execute prefab-script [$name] -- [prefab] is required!")
		if(!prefab instanceof Closure)
			throw new RuntimeException("Cannot execute prefab-script [$name] -- [prefab] is not a closure!")
		
		try {
			
			Context.getEngine().addEntity binding.variables["entity"]
			
			prefab.delegate = this
			prefab()
		} catch (Throwable t) {
			throw new RuntimeException("Cannot execute prefab-script [$name] -- ${t.class.simpleName} : $t.message")
		}
		
		def isPrefab = create(IsFromPrefab)
		isPrefab.name = name
		
		binding.variables["entity"].add isPrefab
		
		print "Inflating prefab \"$name\": "
		binding.variables["entity"].getComponents().forEach { print "[${it.class.simpleName}]" }
		println ""
		
		binding.variables["entity"]
	}
	
	public abstract void scriptBody()
	
	//
	//
	//
	
	public void include(String name) {
		def ps = PrefabScript.byName(name)
		if(ps == null) {
			println "WARNING: Prefab [${binding.variables['label']}] references prefab '$name', which is unknown."
			return
		}
		
		ps.binding.variables['entity'] = this.binding.variables["entity"]
		
		ps.scriptBody()
		
		def prefab = ps.binding.variables["prefab"]
		
		prefab.delegate = this
		prefab()
	}
	
	/**
	 * Create a new instance of the given {@link Component}-type.
	 * @param <T>
	 * @param clazz
	 * @return
	 */
	public <T extends Component> T create(Class<T> clazz) {
		def component = Context.getEngine().createComponent(clazz)
		if (binding.variables['entity'] != null)
			binding.variables['entity'].add component
		component
	}
	
	public <T extends Component> T get(Class<T> clazz) {
		get(binding.variables['entity'], clazz)
	}
	
	public <T extends Component> T get(Entity entity, Class<T> clazz) {
		if(clazz == null || entity == null)
			return null
		
		if(!ComponentMapper.getFor(clazz).has(entity))
			return create(clazz)
		
		ComponentMapper.getFor(clazz).get(entity)
	}
	
	/**
	 * Tag this Entity with the given tag
	 * @param tag
	 */
	public void tag(String tag) {
		if(binding.variables['entity'] == null)
			return
		Context.getEngine().getSystem(UniqueTagManager).set tag, binding.variables['entity']
	}
	
	/**
	 * Get the {@link Entity} associated with the given tag
	 * @param tag
	 * @return
	 */
	public Entity tagged(String tag) {
		if(tag == null)
			return null
		Context.getEngine().getSystem(UniqueTagManager).get tag
	}
	
	/**
	 * Get the {@link SquidID ID} associated with this Entity
	 * @param entity
	 * @return
	 */
	public SquidID id(Entity entity) {
		if(entity == null)
			return null
		Context.getEngine().getSystem(EntityRefManager).get entity
	}
	
	/**
	 * Get the {@link Entity} associated with the given {@link SquidID ID}
	 * @param id
	 * @return
	 */
	public Entity byID(SquidID id) {
		if(id == null)
			return null
		Context.getEngine().getSystem(EntityRefManager).get id
	}
	
	/**
	 * Associate this {@link Entity} with the {@link Faction} denoted by the given name.
	 * @param name
	 */
	public void faction(String name) {
		if(binding.variables['entity'] == null)
			return
		Context.getEngine().getSystem(FactionManager).set Factions.get().getBy(name), binding.variables['entity']
	}
	
	/**
	 * Get the {@link Faction} with which this Entity is associated with.
	 * @return
	 */
	public Faction faction() {
		if(binding.variables['entity'] == null)
			return null
		Context.getEngine().getSystem(FactionManager).get binding.variables['entity']
	}
	
	/**
	 * Provides access to the Entity (if any) tagged with {@link Tags#WORLD_MAP}.
	 * @return
	 */
	public Entity worldMapEntity() {
		tagged(Tags.WORLD_MAP)
	}
	
	/**
	 * Provides access to the {@link HasMap} component (if any) of the {@link #worldMapEntity()}.
	 * @return
	 */
	public HasMap worldMap() {
		get(worldMapEntity(), HasMap)
	}
}
