package com.hndl.ui.utils;

import java.util.Map;

public class MapUtil {

	public static String getString(Map<String,Object> table, String key, String defalut)
	{
		if (table != null && table.containsKey(key)) {
			Object obj = table.get(key);
			if ((obj+"").equals("null")||(obj+"").equals("NULL")||(obj+"").equals("")) {
				obj = defalut;
			}
			if (obj instanceof String) {
				return ((String) obj).trim();
			}else if (obj instanceof Integer) {
				return (obj+"").trim();
			}
		}
		return defalut;
	}

	public static int getInt(Map<String,Object> table, String key, int default_value)
	{
		int ret = default_value;
		try {
			if (table != null && table.containsKey(key)) {
				Object obj = table.get(key);
				if (obj instanceof String) {
					ret = Integer.parseInt((String)obj);
				}else
					ret = (Integer) obj;
			}
		} catch (Exception e) {e.printStackTrace();}
		return ret;
	}

	public static long getLong(Map<String,Object> table, String key, long default_value)
	{
		long ret = default_value;
		try {
			if (table != null && table.containsKey(key)) {
				Object obj = table.get(key);
				if (obj instanceof String) {
					ret = Long.parseLong((String)obj);
				} else
					ret = (Long) obj;
			}
		} catch (Exception e) {e.printStackTrace();}
		return ret;
	}
	
	public static boolean getBoolean(Map<String,Object> table, String key, boolean default_value)
	{
		boolean ret = default_value;
		try {
			if (table != null && table.containsKey(key)) {
				ret = (Boolean) table.get(key);
			}
		}catch(Exception e) {}
		
		return ret;
	}

	public static float getFloat(Map<String,Object> table,String key, float default_value)
	{
		float ret = default_value;
		try {
			if (table != null && table.containsKey(key)) {
				Object obj = table.get(key);
				if (obj instanceof String) {
					ret = Float.parseFloat((String)obj);
				} else
					ret = (Float) obj;
			}
		} catch (Exception e) {e.printStackTrace();}
		return ret;
	}
}
