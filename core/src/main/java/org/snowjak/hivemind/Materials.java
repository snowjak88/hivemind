/**
 * 
 */
package org.snowjak.hivemind;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.snowjak.hivemind.json.Json;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.google.common.io.MoreFiles;
import com.google.gson.annotations.SerializedName;

import squidpony.squidmath.OrderedMap;

/**
 * Repository for {@link Material}s.
 * 
 * @author snowjak88
 *
 */
public class Materials {
	
	private static final Logger LOG = Logger.getLogger(Materials.class.getName());
	private static final String directory = "data" + File.separator + "materials" + File.separator;
	
	private static Materials __INSTANCE = null;
	
	public static Materials get() {
		
		if (__INSTANCE == null)
			synchronized (Materials.class) {
				if (__INSTANCE == null)
					__INSTANCE = new Materials();
			}
		return __INSTANCE;
	}
	
	private final OrderedMap<String, Material> materials = new OrderedMap<>();
	
	private Materials() {
		
		try {
			
			final File dir = Gdx.files.local(directory).file();
			if (!dir.exists())
				dir.mkdirs();
			
			LOG.info("Loading Material definitions from [" + dir.getPath() + "] ...");
			
			for (Path p : MoreFiles.fileTraverser().breadthFirst(dir.toPath())) {
				final File f = p.toFile();
				if (!f.isFile())
					continue;
				
				try (FileReader fr = new FileReader(f)) {
					final Material mat = Json.get().fromJson(fr, Material.class);
					
					if (mat == null) {
						LOG.warning("No Material definition in [" + p.toString() + "]");
						continue;
					}
					
					LOG.info("Loaded Material definition from [" + p.toString() + "]");
					materials.put(mat.name, mat);
				}
			}
			
		} catch (IOException e) {
			LOG.severe("Could not successfully load all Material definitions -- " + e.getClass().getSimpleName() + ": "
					+ e.getMessage());
		}
	}
	
	/**
	 * Get the {@link Material} instance corresponding to the given {@code name}, or
	 * {@code null} if no such correspondence exists.
	 * 
	 * @param name
	 * @return
	 */
	public Material get(String name) {
		
		synchronized (this) {
			return materials.get(name);
		}
	}
	
	/**
	 * Get the {@link Material} corresponding to the given {@code index}, or
	 * {@code null} if no such correspondence exists.
	 * 
	 * @param index
	 * @return
	 */
	public Material get(short index) {
		
		synchronized (this) {
			return materials.getAt(index);
		}
	}
	
	/**
	 * Get the index corresponding to the given Material.
	 * 
	 * @param material
	 * @return -1 if {@code material} == {@code null}
	 */
	public short getIndex(Material material) {
		
		synchronized (this) {
			if (material == null)
				return -1;
			
			if (!materials.containsKey(material.getName()))
				materials.put(material.getName(), material);
			return (short) materials.indexOf(material.getName());
		}
	}
	
	/**
	 * Get the index corresponding to the Material denoted by the given name.
	 * 
	 * @param name
	 * @return
	 */
	public short getIndex(String name) {
		
		synchronized (this) {
			if (!materials.containsKey(name))
				return -1;
			return (short) materials.indexOf(name);
		}
	}
	
	public static class Material {
		
		private String name = null;
		private Color color = null;
		private transient float colorFloat = 0f;
		@SerializedName("visibility-resistance")
		private float visibilityResistance = 0f;
		@SerializedName("additive-visibility")
		private boolean additiveVisibilityResistance = false;
		private float flowSpeed = 0f;
		
		public String getName() {
			
			return name;
		}
		
		public void setName(String name) {
			
			this.name = name;
		}
		
		public Color getColor() {
			
			return color;
		}
		
		public void setColor(Color color) {
			
			this.color = color;
			this.colorFloat = color.toFloatBits();
		}
		
		public float getColorFloat() {
			
			if (colorFloat == 0f)
				colorFloat = color.toFloatBits();
			return colorFloat;
		}
		
		public float getVisibilityResistance() {
			
			return visibilityResistance;
		}
		
		public void setVisibilityResistance(float visibilityResistance) {
			
			this.visibilityResistance = visibilityResistance;
		}
		
		public boolean isAdditiveVisibilityResistance() {
			
			return additiveVisibilityResistance;
		}
		
		public void setAdditiveVisibilityResistance(boolean additiveVisibilityResistance) {
			
			this.additiveVisibilityResistance = additiveVisibilityResistance;
		}
		
		public float getFlowSpeed() {
			
			return flowSpeed;
		}
		
		public void setFlowSpeed(float flowSpeed) {
			
			this.flowSpeed = flowSpeed;
		}
	}
}
