/**
 * 
 */
package org.snowjak.hivemind.behavior.support

import java.nio.file.Path
import java.util.concurrent.Callable
import java.util.concurrent.Future

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.snowjak.hivemind.Context
import org.snowjak.hivemind.RNG
import org.snowjak.hivemind.concurrent.Executor
import org.snowjak.hivemind.engine.Tags
import org.snowjak.hivemind.engine.components.HasMap
import org.snowjak.hivemind.engine.systems.UniqueTagManager

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.badlogic.gdx.ai.btree.Task.Status
import com.badlogic.gdx.ai.btree.branch.Sequence
import com.badlogic.gdx.ai.btree.decorator.Invert
import com.badlogic.gdx.ai.btree.decorator.Repeat
import com.badlogic.gdx.ai.btree.decorator.UntilFail

import com.google.common.io.MoreFiles

import io.github.classgraph.ClassGraph

import squidpony.squidmath.GreasedRegion

/**
 * @author snowjak88
 *
 */
public abstract class BehaviorScript extends Script {
	
	private static final String ROOT_PATH = "data${File.separator}behavior${File.separator}"
	private static final Map<String,BehaviorScript> CACHED_SCRIPTS = new LinkedHashMap<>();
	private static CompilerConfiguration COMPILER_CONFIG = null;
	
	public static Task<Entity> byName(String name) {
		
		if(!CACHED_SCRIPTS.containsKey(name)) {
			
			for(Path p : MoreFiles.fileTraverser().breadthFirst(Gdx.files.local(ROOT_PATH).file.toPath())) {
				def f = p.toFile()
				if(!f.exists())
					continue
				if(!f.isFile())
					continue
				if(!f.name.equalsIgnoreCase(name + ".groovy"))
					continue
				
				if(COMPILER_CONFIG == null) {
					def icz = new ImportCustomizer()
					icz.addImport "GreasedRegion", GreasedRegion.class.name
					icz.addImport "Status", Status.class.name
					icz.addImport "RNG", RNG.class.name
					def sr = new ClassGraph().enableClassInfo().whitelistPackages("org.snowjak.hivemind").scan()
					sr.getClassesImplementing(Component.class.name).filter({!it.isAbstract() && !it.isInterfaceOrAnnotation()}).forEach {
						icz.addImports(it.loadClass().name)
					}
					
					COMPILER_CONFIG = new CompilerConfiguration()
					COMPILER_CONFIG.scriptBaseClass = BehaviorScript.class.name
					COMPILER_CONFIG.compilationCustomizers << icz
				}
				
				def shell = new GroovyShell(this.class.classLoader, new Binding(), COMPILER_CONFIG)
				
				CACHED_SCRIPTS.put name, shell.parse(f)
				
				break;
			}
		}
		
		def bs = CACHED_SCRIPTS.get(name);
		bs?.run()
	}
	
	@Override
	public Task<Entity> run() {
		
		scriptBody()
		
		def label = binding.variables["label"]
		def behavior = binding.variables["behavior"]
		
		behavior
	}
	
	public abstract void scriptBody()
	
	public Task<Entity> from(String name) {
		BehaviorScript.byName(name)
	}
	
	public Task<Entity> invert(Task<Entity> task) {
		new Invert(task)
	}
	
	public Task<Entity> loop(Task<Entity> task){
		new Repeat(task)
	}
	
	@SuppressWarnings("unchecked")
	public Task<Entity> sequence(Task<Entity>... tasks) {
		new Sequence(tasks)
	}
	
	public Task<Entity> untilFail(Task<Entity> task) {
		new UntilFail(task)
	}
	
	//
	//
	//
	
	public Task<Entity> guarded(Task<Entity> guard, Task<Entity> task) {
		task.guard = guard
		task
	}
	
	public Task<Entity> has(List<Class<? extends Component>> allOf = [], List<Class<? extends Component>> oneOf = [], List<Class<? extends Component>> noneOf = []) {
		def family = Family.all(allOf.toArray(new Class<?>[0])).one(oneOf.toArray(new Class<?>[0])).exclude(noneOf.toArray(new Class<?>[0])).get()
		task {
			(family.matches(it)) ? Status.SUCCEEDED : Status.FAILED
		}
	}
	
	//
	//
	//
	
	public Task<Entity> task(Closure exec) {
		new LeafTask<Entity>() {
					
					public prop = new HashMap<>();
					
					{
						exec.delegate = this
					}
					
					@Override
					public Status execute() {
						if(getObject() == null)
							return Status.FAILED
						exec(getObject())
					}
					
					@Override
					protected Task<Entity> copyTo(Task<Entity> task) {
						task
					}
					
					public <T extends Component> T create(Class<T> clazz) {
						if(getObject() == null)
							return null
						
						def c = Context.getEngine().createComponent(clazz)
						getObject().add c
						c
					}
					
					public boolean has(Class<? extends Component> clazz) {
						getObject() != null && ComponentMapper.getFor(clazz).has(getObject())
					}
					
					public <T extends Component> T get(Class<T> clazz) {
						(getObject() == null) ? null : ComponentMapper.getFor(clazz).get(getObject())
					}
					
					public void remove(Class<? extends Component> clazz) {
						if(getObject() != null)
							getObject().remove clazz
					}
					
					public HasMap worldMap() {
						def worldEntity = Context.getEngine().getSystem(UniqueTagManager).get(Tags.WORLD_MAP)
						if(worldEntity == null || !ComponentMapper.getFor(HasMap).has(worldEntity))
							return null
						ComponentMapper.getFor(HasMap).get(worldEntity)
					}
					
					public Future<?> schedule(Closure task) {
						Executor.get().submit(task as Callable<?>)
					}
				}
	}
}
