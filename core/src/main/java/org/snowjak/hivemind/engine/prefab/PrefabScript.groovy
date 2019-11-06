/**
 * 
 */
package org.snowjak.hivemind.engine.prefab

import java.nio.file.Path

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.snowjak.hivemind.Context
import org.snowjak.hivemind.RNG
import org.snowjak.hivemind.engine.Tags
import org.snowjak.hivemind.engine.components.HasMap
import org.snowjak.hivemind.engine.systems.EntityRefManager
import org.snowjak.hivemind.engine.systems.UniqueTagManager
import org.snowjak.hivemind.util.ExtGreasedRegion

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ai.btree.Task.Status
import com.google.common.io.MoreFiles

import io.github.classgraph.ClassGraph
import squidpony.squidgrid.gui.gdx.SColor
import squidpony.squidmath.SquidID

/**
 * @author snowjak88
 *
 */
public abstract class PrefabScript extends Script {
	
	private static final String ROOT_PATH = "data${File.separator}prefab${File.separator}"
	private static final Map<String,PrefabScript> CACHED_SCRIPTS = new LinkedHashMap<>();
	private static CompilerConfiguration COMPILER_CONFIG = null;
	
	public static PrefabScript byName(String name) {
		
		if(!CACHED_SCRIPTS.containsKey(name)) {
			
			def f = Gdx.files.local(ROOT_PATH + name + ".groovy").file;
			if(!f.exists())
				throw new RuntimeException("Cannot load prefab-script [$name] -- does not exist at $f.path")
			if(!f.isFile())
				throw new RuntimeException("Cannot load prefab-script [$name] -- $f.path is not a file")
			
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
			
			CACHED_SCRIPTS.put name, shell.parse(f)
		}
		
		CACHED_SCRIPTS.get(name);
	}
	
	@Override
	public Entity run() {
		
		binding.variables["entity"] = Context.getEngine().createEntity()
		
		scriptBody()
		
		def label = binding.variables["label"]
		def prefab = binding.variables["prefab"]
		
		if(prefab == null)
			throw new RuntimeException("Cannot execute prefab-script -- [prefab] is required!")
		if(!prefab instanceof Closure)
			throw new RuntimeException("Cannot execute prefab-script -- [prefab] is not a closure!")
		
		prefab.delegate = this
		prefab()
		
		Context.getEngine().addEntity binding.variables["entity"]
		
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
	
	public void tag(String tag) {
		if(binding.variables['entity'] == null)
			return null
		Context.getEngine().getSystem(UniqueTagManager).set tag, binding.variables['entity']
	}
	
	public Entity tagged(String tag) {
		if(tag == null)
			return null
		Context.getEngine().getSystem(UniqueTagManager).get tag
	}
	
	public SquidID id(Entity entity) {
		if(entity == null)
			return null
		Context.getEngine().getSystem(EntityRefManager).get entity
	}
	
	public Entity byID(SquidID id) {
		if(id == null)
			return null
		Context.getEngine().getSystem(EntityRefManager).get id
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
