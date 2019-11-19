/**
 * 
 */
package org.snowjak.hivemind.behavior

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.snowjak.hivemind.RNG
import org.snowjak.hivemind.util.ExtGreasedRegion

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ai.btree.Task
import com.badlogic.gdx.ai.btree.Task.Status
import com.badlogic.gdx.ai.btree.branch.DynamicGuardSelector
import com.badlogic.gdx.ai.btree.branch.Sequence
import com.badlogic.gdx.ai.btree.decorator.Invert
import com.badlogic.gdx.ai.btree.decorator.Repeat
import com.badlogic.gdx.ai.btree.decorator.UntilFail

import io.github.classgraph.ClassGraph

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
			
			def f = Gdx.files.local(ROOT_PATH + name + ".groovy").file
			
			if(!f.exists())
				throw new RuntimeException("Cannot load behavior-script [$name] -- does not exist at $f.path")
			if(!f.isFile())
				throw new RuntimeException("Cannot load behavior-script [$name] -- $f.path is not a file")
			
			if(COMPILER_CONFIG == null) {
				def icz = new ImportCustomizer()
				icz.addImport "Region", ExtGreasedRegion.class.name
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
		}
		
		def bs = CACHED_SCRIPTS.get(name);
		bs?.run()
	}
	
	@Override
	public Task<Entity> run() {
		
		scriptBody()
		
		def label = binding.variables["label"]
		def behavior = binding.variables["behavior"]
		
		if(behavior == null) {
			println "[behavior] is null!"
			throw new RuntimeException("Cannot load behavior-script -- [behavior] is required!")
		}
		
		if(!(behavior instanceof Task)) {
			println "[behavior] is not a Task!"
			throw new RuntimeException("Cannot load behavior-script -- [behavior] has not been initialized as a task")
		}
		
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
	
	@SuppressWarnings("unchecked")
	public Task<Entity> dynamic(Task<Entity>... tasks) {
		new DynamicGuardSelector(tasks)
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
		
		new BehaviorCustomLeafTask(exec)
	}
	
	public Task<Entity> task(Map taskSpec) {
		new BehaviorCustomLeafTask(taskSpec['start'], taskSpec['exec'], taskSpec['end'])
	}
}
