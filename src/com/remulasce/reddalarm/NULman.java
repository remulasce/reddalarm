package com.remulasce.reddalarm;

import java.util.ArrayList;

/**Helper class for unpacking data sent in NUL format (Not Understandable Language)
 * 
 * Uses key-pairs of simple strings. Outer values of nested pairs are returned as the entire string constituted
 * within them, so in the case of (a:(b:1)(c:hi)), returned would be key 'B' with '1' (b:1),(c:hi),and (a:(b:1)(c:hi)).
 * So yeah, deep recursion isn't great, but this way is easy to code both sides for.
 * */
public class NULman {
	/** Change this function to desired debug method when including this in your projects. **/
	private static void log(String msg) {
		C.log(msg);
	}
	
	public static ArrayList<KVPair> unpack(String data) {
		ArrayList<KVPair> ret = new ArrayList<KVPair>();
		int iii = 0;
		while (iii < data.length()) {
			//Every time we encounter the start of a new key, jump ahead through the string to find the matched closing paren
			//and add all values in between to that key's value. Then we continue where that key started, so stuff indeed is 
			//counted twice.
			if (data.charAt(iii) == '(') {
				KVPair tmp = new KVPair();
				int lll = iii + 1;
				while (data.charAt(lll) != ':') {
					lll++;
				}
				tmp.Key = data.substring(iii+1, lll);
				int count = 1;
				int jjj = iii + 1;
				while (count > 0) {
					if (data.charAt(jjj) == ')') {
						count--;
					}
					if (data.charAt(jjj) == '(') {
						count++;
					}
					jjj++;
				}
				tmp.Value = data.substring(lll+1,jjj-1);
				ret.add(tmp);				
			}
			iii++;
		}
		return ret;
	}
	/** Find the value of key from NULscript raw. See getNum. **/
	public static String getStr(String key, String raw) {
		ArrayList<KVPair> pairs = unpack(raw);
		for (KVPair each : pairs) {
			if (each.Key.equals(key)) {
				return each.Value;
			}
		}
		log("NUL could not find key: "+key+" from: "+raw);
		throw new RuntimeException
			("NUL language parsin error: Could not find key: "+key+" from: "+raw);
	}
	/** A hacky way to do a general number-getting method. Provide the key of
	 * a value you want from the raw NUL, and it will be returned at an accuracy
	 * probably far surpasing what you actually needed. Only values from the
	 * immediate NUL level will be returned, eg. will not find x from (loc:(x:0)(y:0))
	 * @return the value asssociated with the key, or else throw exception.
	 */
	public static double getNum(String key, String raw) {
		ArrayList<KVPair> pairs = unpack(raw);
		for (KVPair each : pairs) {
			if (each.Key.equals(key)) {
				return Double.parseDouble(each.Value);
			}
		}
		log("NUL could not find key: "+key+" from: "+raw);
		throw new RuntimeException
			("NUL language parsin error: Could not find key: "+key+" from: "+raw);
	}
	/** Unpack a vector2 object, form (x:1)(y:3) **/
	/*
	public static Vector2 unpackVector2(String raw) {
		ArrayList<KVPair> tmp = NULman.unpack(raw);
		Vector2 ret = new Vector2(	Float.parseFloat(tmp.get(0).Value),
									Float.parseFloat(tmp.get(1).Value)
								 );
		return ret;
	} */
	
}
