/**
 * 
 */
package org.snowjak.hivemind.json;

import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.snowjak.hivemind.config.Config;
import org.snowjak.hivemind.util.loaders.Loader;

import com.badlogic.ashley.core.Component;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

/**
 * Provides access to a singleton {@link Gson} instance.
 * 
 * @author snowjak88
 *
 */
public class Json {
	
	private static final Logger LOG = Logger.getLogger(Json.class.getName());
	public static final String PREFRENCE_PRETTY_PRINTING = "json.pretty-print";
	
	private static Json __INSTANCE = null;
	
	public static Json get() {
		
		if (__INSTANCE == null)
			synchronized (Json.class) {
				if (__INSTANCE == null)
					__INSTANCE = new Json();
			}
		
		return __INSTANCE;
	}
	
	private final Gson gson;
	
	@SuppressWarnings("unchecked")
	private Json() {
		
		final GsonBuilder gb = new GsonBuilder();
		
		gb.serializeSpecialFloatingPointValues();
		gb.serializeNulls();
		if (Config.get().getBoolean(PREFRENCE_PRETTY_PRINTING))
			gb.setPrettyPrinting();
		
		final ScanResult sr = new ClassGraph().enableClassInfo().enableAnnotationInfo()
				.whitelistPackages("org.snowjak.hivemind").scan();
		
		for (ClassInfo ci : sr.getClassesImplementing(Loader.class.getName())
				.filter((c) -> !c.isAbstract() && !c.isInterfaceOrAnnotation())) {
			
			//
			// Loader has one type-parameter, definined the type which the Loader is capable
			// of de/serializing.
			//
			final String adaptedType = ci.getTypeSignature().getSuperinterfaceSignatures().stream()
					.filter(ts -> ts.getBaseClassName().equals(Loader.class.getName())).findFirst()
					.map(ts -> ts.getTypeArguments().get(0).toString()).orElse(null);
			
			if (adaptedType == null)
				continue;
			
			try {
				final Constructor<?> typeAdapterConstructor = ci.loadClass().getConstructor();
				
				LOG.info("Registering a new Gson Loader for <" + adaptedType + "> -- " + ci.getName());
				gb.registerTypeAdapter(Class.forName(adaptedType), typeAdapterConstructor.newInstance());
				
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("Cannot instantiate JSON de/serializer -- Loader implementation ("
						+ ci.getName() + ") doesn't have a 0-argument constructor!", e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Cannot instantiate JSON de/serializer -- Loader implementation ("
						+ ci.getName() + ") threw an exception upon construction!", e);
			} catch (InstantiationException e) {
				throw new RuntimeException("Cannot instantiate JSON de/serializer -- Loader implementation ("
						+ ci.getName() + ") cannot be constructed!", e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Cannot instantiate JSON de/serializer -- Loader implementation ("
						+ ci.getName() + ") is not accessible!", e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Cannot instantiate JSON de/serializer -- Loader implementation ("
						+ ci.getName() + ") was not fed appropriate arguments!", e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Cannot instantiate JSON de/serializer -- Loader implementation ("
						+ ci.getName() + ") is not targeting an accessible type!", e);
			}
		}
		
		final RuntimeTypeAdapterFactory<Component> componentTypeAdapterFactory = RuntimeTypeAdapterFactory
				.of(Component.class);
		
		for (ClassInfo ci : sr.getClassesImplementing(Component.class.getName())
				.filter((c) -> !c.isAbstract() && !c.isInterfaceOrAnnotation())) {
			
			LOG.info("Registering the Component sub-type [" + ci.getName() + "] under the type-name '"
					+ ci.getSimpleName() + "'");
			componentTypeAdapterFactory.registerSubtype((Class<? extends Component>) ci.loadClass());
		}
		
		gb.registerTypeAdapterFactory(componentTypeAdapterFactory);
		
		this.gson = gb.create();
	}
	
	/**
	 * 
	 * @param src
	 * @param typeOfSrc
	 * @see Gson#toJson(Object,Type)
	 * @return
	 */
	public String toJson(Object src, Type typeOfSrc) {
		
		return gson.toJson(src, typeOfSrc);
	}
	
	/**
	 * @param src
	 * @param writer
	 * @throws JsonIOException
	 * @see com.google.gson.Gson#toJson(java.lang.Object, java.lang.Appendable)
	 */
	public void toJson(Object src, Appendable writer) throws JsonIOException {
		
		gson.toJson(src, writer);
	}
	
	/**
	 * 
	 * @param src
	 * @param typeOfSrc
	 * @param writer
	 * @see Gson#toJson(Object, Type, Appendable)
	 * @throws JsonIOException
	 */
	public void toJson(Object src, Type typeOfSrc, Appendable writer) throws JsonIOException {
		
		gson.toJson(src, typeOfSrc, writer);
	}
	
	/**
	 * 
	 * @param jsonElement
	 * @param writer
	 * @see Gson#toJson(JsonElement, Appendable)
	 * @throws JsonIOException
	 */
	public void toJson(JsonElement jsonElement, Appendable writer) throws JsonIOException {
		
		gson.toJson(jsonElement, writer);
	}
	
	/**
	 * 
	 * @param <T>
	 * @param json
	 * @param classOfT
	 * @return
	 * @see Gson#fromJson(String, Class)
	 * @throws JsonSyntaxException
	 */
	public <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
		
		return gson.fromJson(json, classOfT);
	}
	
	/**
	 * 
	 * @param <T>
	 * @param json
	 * @param classOfT
	 * @return
	 * @see Gson#fromJson(Reader, Class)
	 * @throws JsonSyntaxException
	 * @throws JsonIOException
	 */
	public <T> T fromJson(Reader json, Class<T> classOfT) throws JsonSyntaxException, JsonIOException {
		
		return gson.fromJson(json, classOfT);
	}
	
	/**
	 * 
	 * @param <T>
	 * @param json
	 * @param classOfT
	 * @return
	 * @see Gson#fromJson(JsonElement, Class)
	 * @throws JsonSyntaxException
	 */
	public <T> T fromJson(JsonElement json, Class<T> classOfT) throws JsonSyntaxException {
		
		return gson.fromJson(json, classOfT);
	}
}
