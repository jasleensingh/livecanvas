package common.typeutils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ResourceManager {

	public static final String STORE_PREFIX = "@";

	protected ResourceBundle bundle;

	protected Locale locale;

	protected String baseName;

	protected Map<String, Object> store = new HashMap<String, Object>();

	public ResourceManager(String baseName) {
		this(baseName, Locale.getDefault());
	}

	public ResourceManager(ResourceBundle bundle) {
		this(bundle, Locale.getDefault());
	}

	public ResourceManager(String baseName, Locale locale) {
		this.locale = locale;
		bundle = ResourceBundle.getBundle(baseName);
	}

	public ResourceManager(ResourceBundle bundle, Locale locale) {
		this.locale = locale;
		this.bundle = bundle;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
		bundle = ResourceBundle.getBundle(baseName);
	}

	public Object getStoredObject(String key) {
		return store.get(key);
	}

	protected boolean checkAndStore(String name, Object obj) {
		if (name.startsWith(STORE_PREFIX)) {
			store.put(name, obj);
			return true;
		}
		return false;
	}

	public boolean contains(String key) {
		try {
			bundle.getString(key);
			return true;
		} catch (MissingResourceException ex) {
			return false;
		}
	}

	public String getString(String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException ex) {
			ex.printStackTrace();
			return "";
		}
	}

	public String[] getStringArray(String key) {
		try {
			return bundle.getString(key).split("[\\s]+");
		} catch (MissingResourceException ex) {
			ex.printStackTrace();
			return new String[0];
		}
	}

	public char getChar(String key) {
		try {
			String s = getString(key);
			if (s.length() != 1) {
				throw new Exception(key + " value not a char");
			}
			return s.charAt(0);
		} catch (Exception ex) {
			ex.printStackTrace();
			return '\0';
		}
	}

	public int getInt(String key) {
		try {
			String s = getString(key);
			return Integer.parseInt(s);
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}
	}

	public boolean getBool(String key) {
		try {
			String s = getString(key);
			return Boolean.parseBoolean(s);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public Icon getIcon(Object baseObject, String key) {
		try {
			String s = getString(key);
			return new ImageIcon(baseObject.getClass().getResource(s));
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
