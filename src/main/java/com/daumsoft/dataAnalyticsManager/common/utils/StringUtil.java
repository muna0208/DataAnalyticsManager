package com.daumsoft.dataAnalyticsManager.common.utils;

import java.io.UnsupportedEncodingException;
import java.lang.Character.UnicodeBlock;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public class StringUtil {
	// private static final Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);

	/**
	 * 한국어를 포함하는지 확인한다.
	 * 
	 * @param s
	 * @return
	 */
	public static boolean hasKorean(String s) {
		if (s == null || s.length() == 0)
			return false;
		char[] ch = s.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			if (isKorean(ch[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 한국어인지 확인한다.
	 * 
	 * @param c
	 * @return
	 */
	public static final boolean isKorean(char c) {
		if (c > 0xD7A3)
			return false;
		else if (c < 0xAC00)
			return false;
		return true;
	}

	/**
	 * 한자를 포함하는지 확인한다.
	 * 
	 * @param s
	 * @return
	 */
	public static boolean hasHanja(String s) {
		if (s == null || s.length() == 0)
			return false;
		char[] ch = s.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			if (isHanja(ch[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 입력 캐릭터가 한자인지 판단한다.
	 * 
	 * @param input 입력 캐릭터
	 * @return 한자인지 여부
	 */
	public static boolean isHanja(char input) {
		boolean isHanjaChk = false;

		Character.UnicodeBlock uniBlk = Character.UnicodeBlock.of(input);
		if (UnicodeBlock.CJK_COMPATIBILITY.equals(uniBlk) || UnicodeBlock.CJK_COMPATIBILITY_FORMS.equals(uniBlk)
				|| UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(uniBlk)
				|| UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT.equals(uniBlk)
				|| UnicodeBlock.CJK_RADICALS_SUPPLEMENT.equals(uniBlk)
				|| UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(uniBlk)
				|| UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(uniBlk)
				|| UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B.equals(uniBlk))
			isHanjaChk = true;
		else {
			isHanjaChk = false;
		}

		return isHanjaChk;
	}

	/**
	 * <p>
	 * trim 적용후 리턴한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.trim(null)    = null
	 * StringUtil.trim("  ")    = ""
	 * StringUtil.trim(" abc ") = "abc"
	 * </pre>
	 * 
	 * @param object (문자형 객체)
	 * @return String (공백제거 후 문자)
	 */
	public static String trim(Object object) {
		return object == null ? null : String.valueOf(object).trim();
	}

	/**
	 * <p>
	 * trim 적용후 리턴한다. null, 공백일 경우 null을 리턴한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.trimToNull(null)    = null
	 * StringUtil.trimToNull("")      = null
	 * StringUtil.trimToNull("  ")    = null
	 * StringUtil.trimToNull(" abc ") = "abc"
	 * </pre>
	 * 
	 * @param object (문자형 객체)
	 * @return String (공백제거 후 문자)
	 */
	public static String trimToNull(Object object) {
		return isBlank(object) ? null : trim(object);
	}

	/**
	 * <p>
	 * 문자 또는 숫자가 비어있는지 체크한다.
	 * </P>
	 * 
	 * <pre>
	 * StringUtil.isEmpty(null)    = true
	 * StringUtil.isEmpty("")      = true
	 * StringUtil.isEmpty("  ")    = false
	 * StringUtil.isEmpty(" bob ") = false
	 * StringUtil.isEmpty(123)     = false
	 * </pre>
	 * 
	 * @param object (문자형, 숫자형 객체)
	 * @return boolean (체크결과)
	 */
	public static boolean isEmpty(Object object) {
		return object == null || String.valueOf(object).length() == 0;
	}

	/**
	 * <p>
	 * 문자 또는 숫자가 공백을 제거후 비어있는지 체크한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.isEmptyTrim(null)    = true
	 * StringUtil.isEmptyTrim("")      = true
	 * StringUtil.isEmptyTrim("  ")    = true
	 * StringUtil.isEmptyTrim(" bob ") = false
	 * StringUtil.isEmptyTrim(123)     = false
	 * </pre>
	 * 
	 * @param object (체크할 문자형, 숫자형 객체)
	 * @return boolean (체크결과)
	 */
	public static boolean isEmptyTrim(Object object) {
		return isEmpty(trim(object));
	}

	/**
	 * <p>
	 * 문자 또는 숫자가 null 이거나, 공백만 있는지 체크한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.isBlank(null)    = true
	 * StringUtil.isBlank("")      = true
	 * StringUtil.isBlank("  ")    = true
	 * StringUtil.isBlank(" bob ") = false
	 * StringUtil.isBlank(123)     = false
	 * </pre>
	 * 
	 * @param object (체크할 문자형, 숫자형 객체)
	 * @return boolean (체크결과)
	 */
	public static boolean isBlank(Object object) {
		if (object == null)
			return true;
		String str = String.valueOf(object);
		if (str.length() == 0)
			return true;
		for (int i = 0; i < str.length(); i++) {
			if ((Character.isWhitespace(str.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * <p>
	 * 문자 또는 숫자에서 Left 에서부터 지정된 길이만큼 문자를 가져온다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.left(null, *)  = ""
	 * StringUtil.left(*, -ve)   = ""
	 * StringUtil.left("", *)    = ""
	 * StringUtil.left("abc", 0) = ""
	 * StringUtil.left("abc", 2) = "ab"
	 * StringUtil.left("abc", 4) = "abc"
	 * StringUtil.left(12345, 3) = "123"
	 * </pre>
	 * 
	 * @param object (가져올 문자형, 숫자형 객체)
	 * @param len    (가져올 길이)
	 * @return String (추출한 결과값)
	 */
	public static String left(Object object, int len) {
		if (object == null)
			return "";
		if (len < 0)
			return "";
		String str = String.valueOf(object);
		if (str.length() <= len) {
			return str;
		} else {
			return str.substring(0, len);
		}
	}

	/**
	 * <p>
	 * 문자 또는 숫자에서 Right 에서부터 지정된 길이만큼 문자를 가져온다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.right(null, *)  = ""
	 * StringUtil.right(*, -ve)   = ""
	 * StringUtil.right("", *)    = ""
	 * StringUtil.right("abc", 0) = ""
	 * StringUtil.right("abc", 2) = "bc"
	 * StringUtil.right("abc", 4) = "abc"
	 * StringUtil.right(12345, 3) = "345"
	 * </pre>
	 * 
	 * @param object (가져올 문자형, 숫자형 객체)
	 * @param len    (가져올 길이)
	 * @return String (추출한 결과값)
	 */
	public static String right(Object object, int len) {
		if (object == null)
			return "";
		if (len < 0)
			return "";
		String str = String.valueOf(object);
		if (str.length() <= len) {
			return str;
		} else {
			return str.substring(str.length() - len);
		}
	}

	/**
	 * <p>
	 * 문자 또는 숫자에서 지정된 위치 에서부터 지정된 길이만큼 문자를 가져온다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.mid(null, *, *)   = ""
	 * StringUtil.mid(*, *, -ve)    = ""
	 * StringUtil.mid("", 0, *)     = ""
	 * StringUtil.mid("abc", 0, 2)  = "ab"
	 * StringUtil.mid("abc", 0, 4)  = "abc"
	 * StringUtil.mid("abc", 2, 4)  = "c"
	 * StringUtil.mid("abc", 4, 2)  = ""
	 * StringUtil.mid("abc", -2, 2) = "ab"
	 * StringUtil.mid(12345, 2, 2)  = "34"
	 * </pre>
	 * 
	 * @param object     (가져올 문자형, 숫자형 객체)
	 * @param startPoint (시작할 위치)
	 * @param len        (가져올 길이)
	 * @return String (추출한 결과값)
	 */
	public static String mid(Object object, int startPoint, int len) {
		if (object == null)
			return "";
		String str = String.valueOf(object);
		if (len < 0 || startPoint > str.length())
			return "";
		if (startPoint < 0)
			startPoint = 0;
		if (str.length() <= (startPoint + len)) {
			return str.substring(startPoint);
		} else {
			return str.substring(startPoint, startPoint + len);
		}
	}

	/**
	 * <p>
	 * 문자 또는 숫자가 null, 공백일 경우 Empty 반환하고, 그 외는 trim 적용후 리턴한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.clean(null)    = ""
	 * StringUtil.clean("")      = ""
	 * StringUtil.clean("  ")    = ""
	 * StringUtil.clean(" abc ") = "abc"
	 * StringUtil.clean(12345)   = "12345"
	 * </pre>
	 * 
	 * @param object (변경할 문자형, 숫자형 객체)
	 * @return String (변경후 문자)
	 */
	public static String clean(Object object) {
		return object == null ? "" : trim(object);
	}

	/**
	 * <p>
	 * 문자 또는 숫자에서 특정 문자를 해당 문자로 전체 변경한다.
	 * </P>
	 * 
	 * <pre>
	 * StringUtil.replaceAll(null, "b", "ee")    = ""
	 * StringUtil.replaceAll("", "b", "ee")      = ""
	 * StringUtil.replaceAll("  ", "b", "ee")    = "  "
	 * StringUtil.replaceAll(" abc ", "b", "ee") = " aeec "
	 * StringUtil.replaceAll(117, "1", "33")     = "33337"
	 * </pre>
	 * 
	 * @param object (변경할 문자형, 숫자형 객체)
	 * @param oldStr (이전문자)
	 * @param newStr (신규문자)
	 * @return String (변경후 문자)
	 */
	public static String replaceAll(Object object, String oldStr, String newStr) {
		if (object == null)
			return "";
		String str = String.valueOf(object);
		if (oldStr == null || newStr == null || oldStr.length() == 0)
			return str;
		int i = str.lastIndexOf(oldStr);
		if (i < 0)
			return str;
		StringBuffer sbuf = new StringBuffer(str);
		while (i >= 0) {
			sbuf.replace(i, (i + oldStr.length()), newStr);
			i = str.lastIndexOf(oldStr, i - 1);
		}
		return sbuf.toString();
	}

	/**
	 * <p>
	 * 문자 또는 숫자가 null, 공백일 경우 새로운 문자를 반환하고, 그 외는 trim 적용후 리턴한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.replaceBlank(null, "xyz")    = "xyz"
	 * StringUtil.replaceBlank("", "xyz")      = "xyz"
	 * StringUtil.replaceBlank("  ", "xyz")    = "xyz"
	 * StringUtil.replaceBlank(" abc ", "xyz") = "abc"
	 * StringUtil.replaceBlank(123, "xyz")     = "123"
	 * </pre>
	 * 
	 * @param object (적용할 문자형, 숫자형 객체)
	 * @param newStr (새로운 문자)
	 * @return String (적용후 문자)
	 */
	public static String replaceBlank(Object object, String newStr) {
		return isBlank(object) ? newStr : trim(object);
	}

	/**
	 * <p>
	 * 문자 또는 숫자가 null, 공백일 경우 새로운 int 형을 반환하고, 그 외는 trim 적용후 int 형으로 변경후 리턴한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.replaceBlank(null, 567)    = 567
	 * StringUtil.replaceBlank("", 567)      = 567
	 * StringUtil.replaceBlank("  ", 567)    = 567
	 * StringUtil.replaceBlank(" 123 ", 567) = 123
	 * StringUtil.replaceBlank(123, 567)     = 123
	 * </pre>
	 * 
	 * @param object (적용할 문자형, 숫자형 객체)
	 * @param newInt (새로운 숫자)
	 * @return int (적용후 숫자)
	 */
	public static int replaceBlank(Object object, int newInt) {
		return isBlank(object) ? newInt : Integer.parseInt(trim(object));
	}

	/**
	 * <p>
	 * 문자 또는 숫자가 null, 공백일 경우 새로운 long 형을 반환하고, 그 외는 trim 적용후 long 형으로 변경후 리턴한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.replaceBlank(null, 567)    = 567
	 * StringUtil.replaceBlank("", 567)      = 567
	 * StringUtil.replaceBlank("  ", 567)    = 567
	 * StringUtil.replaceBlank(" 123 ", 567) = 123
	 * StringUtil.replaceBlank(123, 567)     = 123
	 * </pre>
	 * 
	 * @param object  (적용할 문자형, 숫자형 객체)
	 * @param newLong (새로운 숫자)
	 * @return long (적용후 숫자)
	 */
	public static long replaceBlank(Object object, long newLong) {
		return isBlank(object) ? newLong : Long.parseLong(trim(object));
	}

	/**
	 * <p>
	 * 대소문자 상관없이 두 문자가 일치하는지 체크한다.
	 * </p>
	 * <p>
	 * java.lang.String.equalsIgnoreCase(String anotherString) 확장형으로 null도 비교한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.isEqualsIgnoreCase(null, null)   = true
	 * StringUtil.isEqualsIgnoreCase(null, "abc")  = false
	 * StringUtil.isEqualsIgnoreCase("abc", null)  = false
	 * StringUtil.isEqualsIgnoreCase("abc", "abc") = true
	 * StringUtil.isEqualsIgnoreCase("abc", "ABC") = true
	 * </pre>
	 * 
	 * @param object1 (체크할 문자형 객체1)
	 * @param object2 (체크할 문자형 객체2)
	 * @return boolean (체크결과)
	 */
	public static boolean isEqualsIgnoreCase(Object object1, Object object2) {
		String str1 = (String) object1;
		String str2 = (String) object2;
		return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
	}

	/**
	 * <p>
	 * 문자 또는 숫자에 검색어가 포함되어 있는지 체크한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.isContain(null, *)    = false
	 * StringUtil.isContain(*, null)    = false
	 * StringUtil.isContain("", "")     = false
	 * StringUtil.isContain("abc", "")  = false
	 * StringUtil.isContain(12345, "a") = false
	 * StringUtil.isContain("abc", "a") = true
	 * StringUtil.isContain(12345, "3") = true
	 * </pre>
	 * 
	 * @param object  (체크할 문자형, 숫자형 객체)
	 * @param keyword (검색어)
	 * @return boolean (검색어 포함여부)
	 */
	public static boolean isContain(Object object, String keyword) {
		if (isEmpty(object) || isEmpty(keyword))
			return false;
		return String.valueOf(object).indexOf(keyword) >= 0;
	}

	/**
	 * <p>
	 * 구분자로 되어있는 문자에 검색어가 포함되어 있는지 체크한다.
	 * </P>
	 * 
	 * <pre>
	 * StringUtil.isContain(null, *, *)       = false
	 * StringUtil.isContain(*, null, *)       = false
	 * StringUtil.isContain("", "", *)        = false
	 * StringUtil.isContain("abc", "", *)     = false
	 * StringUtil.isContain("ab#c", "a", "#") = false
	 * StringUtil.isContain("ab#c#", "", "#") = true
	 * StringUtil.isContain("a#bc", "a", "#") = true
	 * </pre>
	 * 
	 * @param object  (체크할 문자형 객체)
	 * @param keyword (검색어)
	 * @param gubun   (구분자)
	 * @return boolean (검색어 포함여부)
	 */
	public static boolean isContain(Object object, String keyword, String gubun) {
		if (object == null || keyword == null)
			return false;
		if (isEmpty(object) && isEmpty(keyword))
			return false;
		StringTokenizer strToken = new StringTokenizer(String.valueOf(object), gubun);
		while (strToken.hasMoreTokens()) {
			if (strToken.nextToken().equals(keyword)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>
	 * 문자 또는 숫자에 구분자로 되어있는 검색어중 하나라도 포함하고 있는지 체크한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.isContainKeywords(null, *, *)         = false
	 * StringUtil.isContainKeywords(*, null, *)         = false
	 * StringUtil.isContainKeywords("", "", *)          = false
	 * StringUtil.isContainKeywords("", "abc", *)       = false
	 * StringUtil.isContainKeywords("abc", "d#e", "#")  = false
	 * StringUtil.isContainKeywords(12345, "d#54", "#") = false
	 * StringUtil.isContainKeywords("abc", "b#d", "#")  = true
	 * StringUtil.isContainKeywords(12345, "d#45", "#") = true
	 * </pre>
	 * 
	 * @param object   (체크할 문자형, 숫자형 객체)
	 * @param keywords (구분자로 되어있는 검색어)
	 * @param gubun    (구분자)
	 * @return boolean (검색어 포함여부)
	 */
	public static boolean isContainKeywords(Object object, String keywords, String gubun) {
		if (object == null || keywords == null)
			return false;
		if (isEmpty(object) && isEmpty(keywords))
			return false;
		StringTokenizer strToken = new StringTokenizer(keywords, gubun);
		while (strToken.hasMoreTokens()) {
			if (isContain(String.valueOf(object), strToken.nextToken())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>
	 * 문자 또는 숫자가 제한된 길이보다 클경우 지정된 위치마다 특정문자 삽입후 리턴한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.insertion(null, *, *, *)    = ""
	 * StringUtil.insertion("", *, *, *)      = ""
	 * StringUtil.insertion("  ", *, *, *)    = "  "
	 * StringUtil.insertion("abc", *, 0, *)   = "abc"
	 * StringUtil.insertion("abc", *, 1, 4)   = "abc"
	 * StringUtil.insertion(12345, *, 1, 5)   = "12345"
	 * StringUtil.insertion("abc", "|", 1, 0) = "a|b|c"
	 * StringUtil.insertion("abc", "|", 2, 2) = "ab|c"
	 * StringUtil.insertion(12345, "|", 2, 4) = "12|34|5"
	 * </pre>
	 * 
	 * @param object      (변경할 문자형, 숫자형 객체)
	 * @param insertStr   (삽입할 문자)
	 * @param insertPoint (삽입할 위치)
	 * @param limitLen    (제한된 길이)
	 * @return String (변경후 문자)
	 */
	public static String insertion(Object object, String insertStr, int insertPoint, int limitLen) {
		if (object == null)
			return "";
		String str = String.valueOf(object);
		if (isBlank(str))
			return str;
		if (insertPoint < 1)
			return str;
		if (str.length() <= limitLen)
			return str;
		int index = 0;
		StringBuffer sBuf = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			if (insertPoint == index) {
				sBuf.append(insertStr);
				index = 0;
			}
			sBuf.append(str.charAt(i));
			++index;
		}
		return sBuf.toString();
	}

	/**
	 * <p>
	 * 특정문자를 소문자로 변경한다.
	 * </P>
	 * 
	 * <pre>
	 * StringUtil.toLowerCase(null)  = ""
	 * StringUtil.toLowerCase("")    = ""
	 * StringUtil.toLowerCase("  ")  = "  "
	 * StringUtil.toLowerCase("aBC") = "abc"
	 * </pre>
	 * 
	 * @param object (변경할 문자형 객체)
	 * @return String (변경후 문자)
	 */
	public static String toLowerCase(Object object) {
		if (object == null)
			return "";
		return String.valueOf(object).toLowerCase();
	}

	/**
	 * <p>
	 * 특정문자를 대문자로 변경한다.
	 * </P>
	 * 
	 * <pre>
	 * StringUtil.toUpperCase(null)  = ""
	 * StringUtil.toUpperCase("")    = ""
	 * StringUtil.toUpperCase("  ")  = "  "
	 * StringUtil.toUpperCase("aBc") = "ABC"
	 * </pre>
	 * 
	 * @param object (변경할 문자형 객체)
	 * @return String (변경후 문자)
	 */
	public static String toUpperCase(Object object) {
		if (object == null)
			return "";
		return String.valueOf(object).toUpperCase();
	}

	/**
	 * <p>
	 * '_' 가 포함되어 있는 문자열을 Camel Case 표기법으로 변경후 리턴한다.
	 * </p>
	 * <p>
	 * '_' 가 포함되어 있지 않고 첫 문자가 대문자인 경우 전체를 소문자로 변경한다.
	 * </p>
	 * <p>
	 * ※ Camel Case : 둘 이상의 단어를 붙여서 쓸때 첫 문자는 소문자로 시작하고 새로 시작하는 단어는 대문자로 표기하는 방식.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.toCamelCase(null)      = ""
	 * StringUtil.toCamelCase("")        = ""
	 * StringUtil.toCamelCase("  ")      = ""
	 * StringUtil.toCamelCase("abc")     = "abc"
	 * StringUtil.toCamelCase("aBc")     = "aBc"
	 * StringUtil.toCamelCase("AbC")     = "abc"
	 * StringUtil.toCamelCase("abc_def") = "abcDef"
	 * </pre>
	 * 
	 * @param object (변경할 문자형 객체)
	 * @return String (변경후 문자)
	 */
	public static String toCamelCase(Object object) {
		if (isBlank(object))
			return "";
		String str = trim(object);
		if (str.indexOf('_') < 0 && Character.isLowerCase(str.charAt(0))) {
			return str;
		}
		StringBuilder result = new StringBuilder();
		boolean nextUpper = false;
		for (int i = 0; i < str.length(); i++) {
			char currentChar = str.charAt(i);
			if (currentChar == '_') {
				nextUpper = true;
			} else {
				if (nextUpper) {
					result.append(Character.toUpperCase(currentChar));
					nextUpper = false;
				} else {
					result.append(Character.toLowerCase(currentChar));
				}
			}
		}
		return result.toString();
	}

	/**
	 * camelcase 의 문자열을 db 필드 포맷으로 변경한다.
	 * 
	 * @param object
	 * @return
	 */
	public static String javaToDB(Object object) {
		if (isBlank(object))
			return "";
		String str = trim(object);
		StringBuilder result = new StringBuilder();
		result.append(Character.toLowerCase(str.charAt(0)));
		for (int i = 1; i < str.length(); i++) {
			char currentChar = str.charAt(i);

			if (Character.isUpperCase(currentChar)) {
				result.append("_");
				result.append(Character.toLowerCase(currentChar));
			} else {
				result.append(Character.toLowerCase(currentChar));
			}
		}
		return result.toString();
	}

	/**
	 * <p>
	 * 쿼리에 사용되는 문자를 표준형("''")으로 변경후 리턴한다.
	 * </P>
	 * 
	 * <pre>
	 * StringUtil.toQueryString(null)   = ""
	 * StringUtil.toQueryString("")     = ""
	 * StringUtil.toQueryString("  ")   = " "
	 * StringUtil.toQueryString("abc")  = "abc"
	 * StringUtil.toQueryString("ab'c") = "ab''c"
	 * </pre>
	 * 
	 * @param object (변경할 문자형 객체)
	 * @return String (변경후 문자)
	 */
	public static String toQueryString(Object object) {
		if (object == null)
			return "";
		String str = String.valueOf(object);
		if (str.length() == 0)
			return str;
		return str.replaceAll("'", "''");
	}

	/**
	 * <p>
	 * 작은따옴표를 ASCII 코드로 변경 후 리턴한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.encodeSingleMark(null)   = ""
	 * StringUtil.encodeSingleMark("")     = ""
	 * StringUtil.encodeSingleMark("  ")   = ""
	 * StringUtil.encodeSingleMark("abc")  = "abc"
	 * StringUtil.encodeSingleMark("ab'c") = "ab&amp;#39;c"
	 * </pre>
	 * 
	 * @param object (변경할 문자형 객체)
	 * @return String (변경후 문자)
	 */
	public static String encodeSingleMark(Object object) {
		if (isBlank(object))
			return "";
		return String.valueOf(object).replaceAll("\'", "&#39;");
	}

	/**
	 * <p>
	 * 쌍따옴표를 ASCII 코드로 변경 후 리턴한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.encodeDoubleMark(null)   = ""
	 * StringUtil.encodeDoubleMark("")     = ""
	 * StringUtil.encodeDoubleMark("  ")   = ""
	 * StringUtil.encodeDoubleMark("abc")  = "abc"
	 * StringUtil.encodeDoubleMark("ab"c") = "ab&amp;#34;c"
	 * </pre>
	 * 
	 * @param object (변경할 문자형 객체)
	 * @return String (변경후 문자)
	 */
	public static String encodeDoubleMark(Object object) {
		if (isBlank(object))
			return "";
		return String.valueOf(object).replaceAll("\"", "&#34;");
	}

	/**
	 * <p>
	 * 작은따옴표, 쌍따옴표를 ASCII 코드로 변경 후 리턴한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.encodeMark(null)   = ""
	 * StringUtil.encodeMark("")     = ""
	 * StringUtil.encodeMark("  ")   = ""
	 * StringUtil.encodeMark("abc")  = "abc"
	 * StringUtil.encodeMark("ab'c") = "ab&amp;#39;c"
	 * StringUtil.encodeMark("ab"c") = "ab&amp;#34;c"
	 * </pre>
	 * 
	 * @param object (변경할 문자형 객체)
	 * @return String (변경후 문자)
	 */
	public static String encodeMark(Object object) {
		if (isBlank(object))
			return "";
		String str = String.valueOf(object);
		str = encodeSingleMark(str);
		str = encodeDoubleMark(str);
		return str;
	}

	/**
	 * <p>
	 * 문자 또는 숫자를 지정된 길이 만큼 자르고 리턴한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.cutString(null, *)  = ""
	 * StringUtil.cutString("", *)    = ""
	 * StringUtil.cutString("  ", *)  = ""
	 * StringUtil.cutString("abc", 3) = "abc"
	 * StringUtil.cutString("abc", 2) = "ab"
	 * </pre>
	 * 
	 * @param object (변경할 문자형, 숫자형 객체)
	 * @param len    (가져올 문자길이)
	 * @return String (변경후 문자)
	 */
	public static String cutString(Object object, int len) {
		if (isBlank(object))
			return "";
		String str = String.valueOf(object);
		if (str.length() > len)
			str = str.substring(0, len);
		return str;
	}

	/**
	 * <p>
	 * 문자 또는 숫자를 지정된 길이 만큼 자르고 접미사 처리후 리턴한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.cutString(null, *, *)      = ""
	 * StringUtil.cutString("", *, *)        = ""
	 * StringUtil.cutString("  ", *, *)      = ""
	 * StringUtil.cutString("abc", 3, *)     = "abc"
	 * StringUtil.cutString("abc", 2, "...") = "ab..."
	 * </pre>
	 * 
	 * @param object (변경할 문자형, 숫자형 객체)
	 * @param len    (가져올 문자길이)
	 * @param suffix (접미사)
	 * @return String (변경후 문자)
	 */
	public static String cutString(Object object, int len, String suffix) {
		if (isBlank(object))
			return "";
		String str = String.valueOf(object);
		if (str.length() > len)
			str = str.substring(0, len) + suffix;
		return str;
	}

	/**
	 * <p>
	 * 문자 또는 숫자를 지정된 길이 만큼 자르고 "..." 처리후 리턴한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.cutTitle(null, *)  = ""
	 * StringUtil.cutTitle("", *)    = ""
	 * StringUtil.cutTitle("  ", *)  = ""
	 * StringUtil.cutTitle("abc", 3) = "abc"
	 * StringUtil.cutTitle("abc", 2) = "ab..."
	 * </pre>
	 * 
	 * @param object (변경할 문자형, 숫자형 객체)
	 * @param len    (가져올 문자길이)
	 * @return String (변경후 문자)
	 */
	public static String cutTitle(Object object, int len) {
		return cutString(object, len, "...");
	}

	/**
	 * <p>
	 * 문자 또는 숫자에서 해당 문자가 몇개인지 체크후 리턴한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.countMatches(null, *)    = 0
	 * StringUtil.countMatches("", *)      = 0
	 * StringUtil.countMatches(*, null)    = 0
	 * StringUtil.countMatches(*, "")      = 0
	 * StringUtil.countMatches("abb", "b") = 2
	 * </pre>
	 * 
	 * @param object  (검색할 문자형, 숫자형 객체)
	 * @param keyword (검색어)
	 * @return int (메칭되는 수)
	 */
	public static int countMatches(Object object, String keyword) {
		if (isEmpty(object) || isEmpty(keyword))
			return 0;
		int count = 0;
		int idx = 0;
		while ((idx = String.valueOf(object).indexOf(keyword, idx)) != -1) {
			count++;
			idx += keyword.length();
		}
		return count;
	}

	/**
	 * <p>
	 * 문자 또는 숫자 앞에 0을 붙여서 원하는 길이의 문자를 리턴한다.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.addZero(null, *)  = ""
	 * StringUtil.addZero("", *)    = ""
	 * StringUtil.addZero("  ", *)  = ""
	 * StringUtil.addZero("123", 2) = "123"
	 * StringUtil.addZero("123", 3) = "123"
	 * StringUtil.addZero("123", 5) = "00123"
	 * </pre>
	 * 
	 * @param object (변경할 문자형, 숫자형 객체)
	 * @param len    (가져올 길이)
	 * @return String (변경후 값)
	 */
	public static String addZero(Object object, int len) {
		if (isBlank(object))
			return "";
		String str = String.valueOf(object);
		String zeroNum = "";
		if (str.length() < len) {
			for (int i = 0; i < (len - str.length()); i++) {
				zeroNum += "0";
			}
		}
		return zeroNum + str;
	}

	public static boolean isNull(String s) {
		if ((s == null) || "".equals(s) || (s.length() < 1)) {
			return true;
		}
		return false;
	}

	public static String getDate(int addDate) {
		DecimalFormat df = new DecimalFormat("00");
		Calendar currentCalendar = Calendar.getInstance();
		currentCalendar.add(Calendar.DATE, addDate);
		String strYear = Integer.toString(currentCalendar.get(Calendar.YEAR));
		String strMonth = df.format(currentCalendar.get(Calendar.MONTH) + 1);
		String strDay = df.format(currentCalendar.get(Calendar.DATE));
		String strDate = strYear + strMonth + strDay;
		return strDate;
	}

	public static String getToday() {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DATE);
		return year + String.format("%02d", month) + String.format("%02d", day);
	}

	public static boolean isNumber(char c) {
		if ((c > '0') && (c < '9'))
			return true;
		else
			return false;
	}

	public static boolean isNumber(String s) {
		char c = '0';
		if (isNull(s))
			return false;
		int len = s.length();
		for (int i = 0; i < len; i++) {
			c = s.charAt(i);
			if ((c < '0') || (c > '9')) {
				return false;
			}
		}
		return true;
	}

	public static int convertInt(String s) {
		if (isNumber(s)) {
			return Integer.parseInt(s);
		}
		return 0;
	}

	public static long convertLong(String s) {
		if (isNumber(s)) {
			return Long.parseLong(s);
		}

		return 0L;
	}

	public static String convertDate(Date dt, SimpleDateFormat sdf) {
		if (dt == null) {
			return "";
		}

		try {
			return sdf.format(dt);
		} catch (Exception e) {
			return "";
		}
	}

	public static boolean convertBoolean(String s) {
		if (s == null) {
			return false;
		}
		if (s.length() <= 0) {
			return false;
		}

		try {
			if (s.equals("true"))
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * src가 null거나 ""이면 tgt값으로 치환하여 return한다
	 */
	public static String nvl(Object src, String tgt) {
		String res = nvl(src);
		if (res.equals(""))
			return tgt;
		return res;
	}

	public static String nvl(String src, String tgt) {
		if (src == null || src.equals(""))
			return tgt;
		else
			return src;
	}

	public static String nvlWithLastCharDel(String src, char c, String tgt) {
		if (src == null || src.equals(""))
			return tgt;

		if (src.charAt(src.length() - 1) == c) {
			src = src.substring(0, src.length() - 1).trim();
		}

		return nvl(src, tgt);
	}

	/**
	 * 처음 나타나는 oldStr를 newStr로 바꾼다.
	 * 
	 * @param originalStr 원래의 문자열.
	 * @param oldStr      바꾸고자하는 문자열.
	 * @param newStr      새로운 문자열.
	 * @return originalStr 문자열에서 모든 oldStr 문자열을 newStr 문자열로 대치한 문자열을 넘긴다.
	 */
	public static String replace(String originalStr, String oldStr, String newStr) {
		return replace(originalStr, oldStr, newStr, 0);
	}

	/**
	 * 처음 나타나는 originalStr를 newStr로 바꾼다.
	 * 
	 * @param originalStr  원래의 문자열.
	 * @param oldStr       바꾸고자하는 문자열.
	 * @param newStr       새로운 문자열.
	 * @param replaceCount originalStr의 처음부터 몇개까지의 oldStr를 newStr로 바꿀지를 결정한다. 1이면 처음
	 *                     나타나는 oldStr 문자열만을 newStr로 바꾼다. 0이면 모든 oldStr 문자열을 newStr로
	 *                     바꾼다.
	 * @return originalStr 문자열에서 oldStr 문자열을 찾아 replaceCount 갯수만큼 newStr 문자열로 대치한
	 *         문자열을 넘긴다.
	 */
	public static String replace(String originalStr, String oldStr, String newStr, int replaceCount) {
		String result = originalStr;
		int index = 0;
		int fromIndex = 0;
		int newLength = newStr.length();
		int oldLength = oldStr.length();
		int i = 0;

		while (true) {
			// System.out.println(String.format("oldstr=[%1$s], fromIndex[%2$s],
			// src[%3$s]",oldStr,
			// fromIndex,
			// originalStr));

			index = result.indexOf(oldStr, fromIndex);

			if (index < 0) {
				return result;
			} else if (index == 0) {
				result = newStr + result.substring(oldLength);
			} else {
				result = result.substring(0, index) + newStr + result.substring(index + oldLength);
			}

			if ((replaceCount != 0) && (++i == replaceCount)) {
				return result;
			}

			fromIndex = index + newLength;
		}
	}

	/**
	 * src가 null거나 ""이면 ""으로 치환하여 return한다
	 */
	public static String nvl(Object src) {
		if (src == null || src.toString().trim().equals(""))
			return "";
		else
			return src.toString();
	}

	public static String[] stringTokenizer(String source, String delimiter) {
		if (source == null || delimiter == null) {
			throw new IllegalArgumentException();
		}

		StringTokenizer st = new StringTokenizer(source, delimiter, false);
		int tokenNumber = st.countTokens();
		if (tokenNumber == 0) {
			return null;
		}

		int i = 0;
		String[] arrResult = new String[tokenNumber];

		while (st.hasMoreTokens()) {
			arrResult[i++] = st.nextToken();
		}

		return arrResult;
	}

	public static boolean isEnglish(char c) {
		boolean flg = true;
		if (c >= '0' && c <= '9') {
			flg = true;
		} else if (c >= 'A' && c <= 'Z') {
			flg = true;
		} else if (c >= 'a' && c <= 'z') {
			flg = true;
		} else {
			return false;
		}
		return flg;
	}

	public static boolean isEnglish(String s) {
		boolean flg = true;
		char c = 'a';
		final int len = s.length();
		for (int i = 0; i < len; i++) {
			c = s.charAt(i);
			if (c >= '0' && c <= '9') {
				flg = true;
			} else if (c >= 'A' && c <= 'Z') {
				flg = true;
			} else if (c >= 'a' && c <= 'z') {
				flg = true;
			} else {
				return false;
			}
		}
		return flg;
	}

	/**
	 * String을 int값으로 변환한다. <BR>
	 * int cnt = StringUtil.stoi("10"); <BR>
	 * 
	 * @param str int값으로 변환될 String 문자열.
	 * @return 변환된 int 값.
	 */
	public static int stoi(String str) {
		if (str == null)
			return 0;
		return (Integer.valueOf(str).intValue());
	}

	/**
	 * int값을 String으로 변환한다. <BR>
	 * 예) Strint str = StringUtil.itos(10); <BR>
	 * 
	 * @param i String으로 변환될 int 값.
	 * @return 변환된 String 값.
	 */
	public static String itos(int i) {
		return Integer.toString(i);
	}

	public static double stod(String str) {
		if (str == null)
			return 0;
		return (Double.parseDouble(str));
	}

	/**
	 * 데이타베이스로 문자열을 저장하기 전에 변환 (주의) 한글변환을 위하여 사용
	 * 
	 * @param str DB로 저장할 한글이 들어있는 문자열
	 * @return tmpStr 8859_1 codeset으로 변환된 문자열
	 */
	public static String toDB(String str) {
		String tmpStr = new String();
		try {
			return Ksc2Uni(str);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(System.err);
		}

		return tmpStr;

	}

	/**
	 * 데이타베이스로부터 얻은 문자열을 변환 (주의) 한글변환을 위하여 사용함
	 * 
	 * @param str DB에서 가져온 한글이 들어있는 문자열
	 * @return tmpStr KSC5601 codeset으로 변환된 문자열
	 */
	public static String fromDB(String str) {
		String tmpStr = new String();
		try {
			tmpStr = ksc2Utf(str);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(System.err);
		}

		return tmpStr;
	}

	/**
	 * Ksc5601 ---> 8859_1 문자열로 변환 예) Strint str = StringUtil.Ksc2Uni("제목:\n"); <BR>
	 * 
	 * @param KscStr
	 * @return String
	 */
	public static String Ksc2Uni(String KscStr) throws java.io.UnsupportedEncodingException {
		if (KscStr == null) {
			return null;
		} else {
			return new String(KscStr.getBytes("KSC5601"), "8859_1");
		}
	}

	/**
	 * 8859_1 ---> Ksc5601 문자열로 변환
	 * 
	 * @param UnicodeStr
	 * @return String
	 */
	public static String Uni2Ksc(String UnicodeStr) throws UnsupportedEncodingException {
		if (UnicodeStr == null) {
			return null;
		} else {
			return new String(UnicodeStr.getBytes("8859_1"), "KSC5601");
			// return UnicodeStr;
		}
	}

	/**
	 * uni ---> U 문자열로 변환
	 * 
	 * @param UnicodeStr
	 * @return String
	 * @author hwado
	 */
	public static String Euc2Utf(String UnicodeStr) throws UnsupportedEncodingException {
		if (UnicodeStr == null) {
			return null;
		} else {
			return new String(UnicodeStr.getBytes("EUC-KR"), "UTF-8");
			// return UnicodeStr;
		}
	}

	public static String ksc2Utf(String str) throws UnsupportedEncodingException {
		if (str == null) {
			return null;
		} else {
			return new String(str.getBytes("KSC5601"), "UTF-8");
			// return UnicodeStr;
		}
	}

	/**
	 * 소숫점 자리수를 조작한다. 예) 10.245 --> 10.24, 9 --> 9.00
	 * 
	 * @param strSrc 변경할 숫자 문자열
	 * @param len    소숫점 자리수
	 * @return 변경된 숫자 문자열
	 */
	public static String setDecimal(String strSrc, int len) {
		int iLen = strSrc.length();
		int iDec = strSrc.indexOf('.');

		StringBuffer convert = new StringBuffer();
		convert.append((iDec < 0) ? strSrc : strSrc.substring(0, iDec));

		if (iDec < 0)
			iDec = iLen;

		for (int i = 1; i <= len; i++) {
			if (i == 1)
				convert.append(".");
			convert.append(((i + iDec) < iLen) ? strSrc.charAt(i + iDec) : '0');
		}
		return convert.toString();
	}

	/**
	 * 소숫점 자리수를 조작한다. 예) 10.245 --> 10.24, 9 --> 9.00
	 * 
	 * @param double 변경할 숫자
	 * @param len    소숫점 자리수
	 * @return 변경된 숫자 문자열
	 */
	public static String setDecimal(double dNumber, int len) {
		return setDecimal(Double.toString(dNumber), len);
	}

	// /**
	// * 입력받은 String에서 Comma를 제거하는 함수
	// *
	// * @param src
	// * Comma로 분할된 String
	// * @return Comma가 제거된 String
	// */
	// static public String wonRetrans(String src) {
	// String resultValue = "";
	//
	// for (int i = 0; i < src.length(); i++) {
	// char temp = src.charAt(i);
	//
	// if (temp == ',')
	// continue;
	//
	// resultValue = resultValue + temp;
	// }
	//
	// return resultValue;
	// }

	/**
	 * 입력받은 String에서 Comma를 제거하는 함수
	 * 
	 * @param src Comma로 분할된 String
	 * @return Comma가 제거된 String
	 */
	static public String wonRetrans(String src) {
		StringBuilder resultValue = new StringBuilder();

		for (int i = 0; i < src.length(); i++) {
			char temp = src.charAt(i);

			if (temp == ',')
				continue;

			resultValue.append(temp);
		}

		return resultValue.toString();
	}

	/**
	 * 입력받은 String을 원하는 길이만큼 원하는 문자로 오른쪽을 채워주는 함수
	 * 
	 * @param calendar 입력받은 String
	 * @return 지정된 문자로 채워진 String
	 */
	static public String rpad(String str, int len, char pad) {
		String result = str;
		int templen = len - result.getBytes().length;

		for (int i = 0; i < templen; i++) {
			result = result + pad;
		}

		return result;
	}

	/**
	 * 입력받은 String을 원하는 길이만큼 원하는 문자로 왼쪽을 채워주는 함수
	 * 
	 * @param calendar 입력받은 String
	 * @return 지정된 문자로 채워진 String
	 */
	static public String lpad(String str, int len, char pad) {
		String result = str;
		int templen = len - result.getBytes().length;

		for (int i = 0; i < templen; i++)
			result = pad + result;

		return result;
	}

	/**
	 * 문자열중 특정문자를 제거한다 (예) 2001/01/01 ==> 20010101
	 * 
	 * @param str 대상문자열
	 * @param tok 제가할 문자열
	 * @return 완성된 문자열
	 */
	public static String remove(String str, String tok) {
		return replace(str, tok, "");
	}

	/*
	 * int i=0;
	 * 
	 * 입력받은 문자열 중 size(byte)를 넘지 않도록 계산해서 해당 문자열을 돌려줌.
	 * 
	 * @param str 문자열
	 * 
	 * @param size byte수
	 * 
	 * @return 문자열 중 size(byte)를 넘지않는 문자열을 돌려줌
	 */
	static public String getLeftHan(String str, int size) {
		String ch;
		int cnt = 0, pos = 0;

		while ((pos < str.length()) && (cnt < size - 1)) {

			ch = str.substring(pos, pos + 1);

			// 한글이 아닌 경우 뒤의 2byte가 0값으로 나온다.
			if (ch.getBytes().length == 2)
				cnt = cnt + 2;
			else
				cnt = cnt + 1;

			pos = pos + 1;
			// System.out.println("ch=" + ch + ",cnt=" + cnt + ",pos=" + pos);

		}

		// System.out.println ("size =" + cnt);
		return str.substring(0, pos);

	}

	/**
	 * 입력받은 문자열을 잘라내어 돌려줌. 문자열이 size 보다 모자라는 경우는 문자열 자체를 돌려줌.
	 * 
	 * @param str  문자열
	 * @param size 문자열 수
	 * @return 문자열을 size 만큼 잘라내어 돌려줌.
	 */
	static public String getLeft(String str, int size) {
		String res = "";
		if (str != null) {
			if (str.length() > size) {
				res = str.substring(0, size);
			} else {
				res = str;
			}
		}
		return res;

	}

	static public String getLeftRad(String str, int size) {
		String res = "";
		if (str == null || str.length() < 1) {
			return "";
		}

		if (str != null) {
			if (str.length() > size) {
				res = str.substring(0, size) + "...";
			} else {
				res = str;
			}
		}
		return res;
	}

	/**
	 * 입력받은 문자열을 잘라내어 돌려줌. 문자열이 size 보다 모자라는 경우는 문자열 자체를 돌려줌.
	 * 
	 * @param str  문자열
	 * @param size 문자열 수
	 * @return 문자열을 size 만큼 잘라내고 size-1에 ".." 붙여서 돌려줌.
	 */
	static public String getLeft2(String str, int size) {
		String res = "";
		String ch = "";
		int size2 = 0;
		if (str == null || str.length() < 1) {
			return "";
		}
		ch = str.substring(0, 1);

		// 한글이 아닌 경우 뒤의 2byte가 0값으로 나온다.
		if (ch.getBytes().length == 2)
			size2 = size;
		else
			size2 = size * 3 / 2;

		if (str != null) {
			if (str.length() > size2) {
				res = str.substring(0, size2 - 1) + "..";
			} else {
				res = str;
			}
		}
		return res;
	}

	/**
	 * 입력받은 문자열을 잘라내어 돌려줌. 문자열이 size 보다 모자라는 경우는 문자열 자체를 돌려줌.
	 * 
	 * @param str  문자열
	 * @param size 문자열 수
	 * @return 문자열을 size 만큼 잘라내어 돌려줌.
	 */
	static public String getRight(String str, int size) {
		String res = "";
		if (str != null) {
			int len = str.length();
			if (len > size) {
				res = str.substring(len - size, len);
			} else {
				res = str;
			}
		}
		return res;

	}

	/*
	 * 스트링을 스페이스로 분리에 배열에 넣어준다
	 * 
	 * @param src : 원본문자열
	 * 
	 * @return String[]
	 */
	public static String[] split(String src) {
		return split(src, " ", false);
	}

	/*
	 * 스트링을 구분자를 이용해 분리해 문자열배열에 넣어준다.
	 * 
	 * @param src : 원본 문자열
	 * 
	 * @param mark : 구분자
	 * 
	 * @return String[]
	 */
	public static String[] split(String src, String mark) {
		return split(src, mark, false);
	}

	/*
	 * 스트링을 구분자를 이용해 분리해 문자열배열에 넣어준다.
	 * 
	 * @param src : 원본 문자열
	 * 
	 * @param mark : 구분자
	 * 
	 * @param btrim : 문자열을 trim 하면 true, 아니면 false(default=false);
	 * 
	 * @return String[]
	 */
	public static String[] split(String src, String mark, boolean btrim) {
		// 처음이 mark 으로 시작할때
		// 중간에 mark이 연속나올때
		// 마지막이 mark으로 끝날때
		String[] arrRet = null;
		ArrayList<String> al = null;
		try {

			StringTokenizer st = new StringTokenizer(src, mark, true);

			al = new ArrayList<String>();

			String prevToken = "";
			int cnt = st.countTokens();

			for (int i = 0, j = 0; i < cnt; i++) {
				String strToken = st.nextToken();
				if (i == 0) {
					if (mark.equals(strToken)) {
						prevToken = strToken;
						strToken = "";
					}
				} else {
					if (mark.equals(strToken)) {
						if (mark.equals(prevToken)) {
							if (i == (cnt - 1)) {
								al.add(j, "");
								j++;
							}

							prevToken = strToken;
							strToken = "";
						} else {
							prevToken = strToken;
							continue;
						}
					} else {
						prevToken = strToken;
					}

				}
				strToken = btrim ? strToken.trim() : strToken;
				al.add(j, strToken);
				j++;

			}
			arrRet = new String[al.size()];
			arrRet = (String[]) al.toArray(arrRet);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return arrRet;
	}

	/*
	 * 스트링을 구분자를 이용해 분리하여 num 번째 값을 리턴한다.
	 * 
	 * @param src : 원본 문자열
	 * 
	 * @param mark : 구분자
	 * 
	 * @param num : 필요한 값의 배열 번호
	 * 
	 * @return String
	 */
	public static String split(String src, String mark, int num) {
		String resultValue = "";
		if (src == null || src.equals("")) {
		} else {
			String[] strArr = split(src, mark);
			if (strArr.length >= (num + 1)) {
				resultValue = strArr[num];
			}
		}
		return resultValue;
	}

	/**
	 * 문자열 내부에서 해당 키워드로 배열로 묶음
	 * 
	 * @param String statement, String cha1, String cha2
	 * @return array
	 * @author 이정동
	 * @date 2007-06-06
	 */
	public static String[] parse(String statement, String cha1, String cha2) {
		Vector<String> strings = new Vector<String>();

		int startIndex = 0;
		int endIndex = 0;
		for (int inx = 0; inx < statement.length(); inx++) {
			if (statement.substring(inx, inx + 1).equals(cha1)) {
				startIndex = inx + 1;
			}
			if (statement.substring(inx, inx + 1).equals(cha2)) {
				endIndex = inx;
				strings.add(statement.substring(startIndex, endIndex));
			}
		}
		String[] returnStrings = new String[strings.size()];
		strings.toArray(returnStrings);
		return returnStrings;
	}

	/**
	 * 문자열 내부에서 해당 키워드로 배열로 묶음
	 * 
	 * @param String statement, String cha1, String cha2
	 * @return array
	 * @author 이정동
	 * @date 2007-06-06
	 */
	public static String parse(String statement, String cha1, String cha2, int num) {
		Vector<String> strings = new Vector<String>();
		//
		// System.out.println("statement=" + statement);
		// System.out.println("cha1=" + cha1);
		// System.out.println("cha2=" + cha2);
		// System.out.println("num=" + num);

		int startIndex = 0;
		int endIndex = 0;
		for (int inx = 0; inx < statement.length(); inx++) {
			if (statement.substring(inx, inx + 1).equals(cha1)) {
				startIndex = inx + 1;
			}
			if (statement.substring(inx, inx + 1).equals(cha2)) {
				endIndex = inx;
				strings.add(statement.substring(startIndex, endIndex));
			}
		}
		String[] returnStrings = new String[strings.size()];
		strings.toArray(returnStrings);
		if (returnStrings.length > 1) {
			return returnStrings[num - 1].toString();
		} else {
			return returnStrings[num].toString();
		}
	}

	/**
	 * field_value에 token 주위의 공백을 제거
	 * 
	 * @param String field_value, String token, char gubun
	 * @return String
	 * @author 이정동
	 * @date 2008-01-04
	 */

	public static String SpaceJob(String field_value, String token, char gubun) {

		int cTempPos = 0; // 토큰 현재포지션
		String lfield_value = ""; // field source에서 작업할 left string
		String rfield_value = ""; // field source에서 작업할 right string

		switch (gubun) {
			case 'p':
				while (field_value.length() > cTempPos) {
					cTempPos = field_value.indexOf(token, cTempPos);
					if (cTempPos != -1) { // token이 존재하면
						if (field_value.substring(cTempPos - 1, cTempPos).equals(" ")) { // token 이전 문자가 공백이면
							lfield_value = field_value.substring(0, cTempPos - 1);
							rfield_value = field_value.substring(cTempPos);
							field_value = lfield_value + rfield_value;
						} else {
							cTempPos++;
						}
					} else
						break;
				}
				break;
			case 'n':
				while (field_value.length() > cTempPos) {
					cTempPos = field_value.indexOf(token, cTempPos);
					if (cTempPos != -1) { // token이 존재하면
						if (field_value.substring(cTempPos + 1, cTempPos + 2).equals(" ")) { // token 이후 문자가 공백이면
							lfield_value = field_value.substring(0, cTempPos + 1);
							rfield_value = field_value.substring(cTempPos + 2);
							field_value = lfield_value + rfield_value;
						} else {
							cTempPos++;
						}
					} else
						break;
				}
				break;
			case 'a':
				cTempPos = 0;
				while (true) {
					cTempPos = field_value.indexOf(token, cTempPos);
					if (cTempPos != -1) { // token이 존재하면
						if ((field_value.length() > cTempPos + 1)
								&& field_value.substring(cTempPos + 1, cTempPos + 2).equals(" ")) { // token이후
																									// 문자가
																									// 공백이면
							lfield_value = field_value.substring(0, cTempPos + 1);
							rfield_value = field_value.substring(cTempPos + 2);
							field_value = lfield_value + rfield_value;
						} else if ((cTempPos > 1) && (field_value.substring(cTempPos - 1, cTempPos).equals(" "))) { // token이전
																													// 문자가
																													// 공백이면
							lfield_value = field_value.substring(0, cTempPos - 1);
							rfield_value = field_value.substring(cTempPos);
							field_value = lfield_value + rfield_value;
						} else {
							cTempPos++;
						}

					} else
						break;
				}
				break;
		}
		return field_value;
	}

	/**
	 * 문자열에서 두개이상의 공백을 하나의 공백으로 처리
	 * 
	 * @param String TargetSourceString
	 * @return String
	 * @author 이정동
	 * @date 2008-01-04
	 */
	public static String OnlyOneSpace(String TargetSourceString) {
		String AfterString = "";
		TargetSourceString = TargetSourceString.trim();
		for (int cnt = 0; cnt < TargetSourceString.length(); cnt++) {
			switch (TargetSourceString.charAt(cnt)) {
				case ' ':
					if (cnt < TargetSourceString.length()) {
						if (TargetSourceString.charAt(cnt + 1) != ' ')
							AfterString += TargetSourceString.charAt(cnt);
					}
					break;
				default:
					AfterString += TargetSourceString.charAt(cnt);
					break;
			}

		}

		return AfterString;
	}

	/**
	 * 문자열에서 특정 문자를 다른문자로 대치시키는 함수
	 * 
	 * @param String Str,char BeforeChar,char AfterChar
	 * @return String
	 * @author 이정동
	 * @date 2008-01-04
	 */
	public static String CharReplace(String Str, char BeforeChar, char AfterChar) {

		String AfterString = "";
		for (int cnt = 0; cnt < Str.length(); cnt++) {
			if (Str.charAt(cnt) == BeforeChar) {
				AfterString += AfterChar;
			} else {
				AfterString += Str.charAt(cnt);
			}
		}

		return AfterString;
	}

	/**
	 * 저장시 특수문자 처리
	 * 
	 * @param String Str
	 * @return String
	 * @author 진승일
	 * @date 2008-01-04
	 */

	public static String SaveSpecialChars(String str) {
		String retStr = "";
		retStr = str.replaceAll("%2C", ",");
		retStr = str.replaceAll("%22", "\"");
		retStr = str.replaceAll("%27", "'");
		return retStr;
	}

	/**
	 * 문자열에서 태그 제거
	 * 
	 * @param String Str
	 * @return String
	 * @author hwado@futureinfonet.com
	 * @date 2008-01-04
	 */
	public static String removeTag(String str) {

		// 정규식 표현 미리 정의해놓음
		Pattern HTML_SCRIPT = Pattern.compile("\\<script[^>]*?>.*?\\<\\/script\\>", Pattern.CASE_INSENSITIVE);
		Pattern HTML_STYLE = Pattern.compile("\\<style[^>]*?>.*?\\<\\/style\\>", Pattern.CASE_INSENSITIVE);
		Pattern HTML_OPTION = Pattern.compile("\\<option[^>]*?>.*?\\<\\/option\\>", Pattern.CASE_INSENSITIVE);
		Pattern HTML_HEAD = Pattern.compile("\\<head\\>(.*?)\\<\\/head\\>", Pattern.CASE_INSENSITIVE);
		Pattern HTML_TAG = Pattern.compile("\\<.*?\\>", Pattern.CASE_INSENSITIVE);

		str = HTML_SCRIPT.matcher(str).replaceAll("");
		str = HTML_STYLE.matcher(str).replaceAll("");
		str = HTML_OPTION.matcher(str).replaceAll("");
		str = HTML_HEAD.matcher(str).replaceAll("");
		str = HTML_TAG.matcher(str).replaceAll("");

		str = str.trim();

		return str;
	}

	/*
	 * // 재귀호출을 사용하여 태그를 제거하면서 오류가 발생하는 것 같아서 주석 처리 함 // 해당 작업을 대신할 함수를 작성함. //
	 * 2008.10.17 public static String removeTag(String str){ int lt =
	 * str.indexOf("<"); if ( lt != -1 ) { int gt = str.indexOf(">", lt); if ( (gt
	 * != -1) ) { str = str.substring( 0, lt ) + str.substring( gt + 1 ); // 재귀 호출
	 * str = removeTag(str); }else{ str = str.substring( 0, lt ); } } return str; }
	 */

	/**
	 * Data형 문자열을 Display용으로 변환 (20080101 --> 2008.01.01)
	 * 
	 * @param str
	 * @return String
	 * @date 2008-02-11
	 */
	public static String DateToDot(String str) {
		String returnstr = "";

		if (str == null)
			str = "";
		if (str.trim().length() < 8)
			return str;

		str = str.trim();

		returnstr = str.substring(0, 4) + ".";
		returnstr += str.substring(4, 6) + ".";
		returnstr += str.substring(6, 8);

		return returnstr;
	}

	/**
	 * Data형 문자열을 Display용으로 변환 (20080101 --> 2008-01-01)
	 * 
	 * @param str
	 * @return String
	 * @date 2008-02-11
	 */
	public static String DateHyPhen(String str) {
		String returnstr = "";

		if (str == null)
			str = "";
		if (str.trim().length() < 8)
			return str;

		if (str.indexOf("-") > 0)
			return str;

		str = str.trim();

		returnstr = str.substring(0, 4) + "-";
		returnstr += str.substring(4, 6) + "-";
		returnstr += str.substring(6, 8);

		return returnstr;
	}

	public static String DateToDot(String str, String dechar) {
		String returnstr = "";

		if (str == null)
			str = "";
		if (str.trim().length() < 8)
			return str;

		str = str.trim();

		returnstr = str.substring(0, 4) + dechar;
		returnstr += str.substring(4, 6) + dechar;
		returnstr += str.substring(6, 8);

		return returnstr;
	}

	public static String ToUpperOneChar(String s) {

		String space = " ";
		String word = "";
		String writing = "";
		StringTokenizer st = new StringTokenizer(s, space);

		while (st.hasMoreTokens()) {
			word = st.nextToken();
			writing += word.substring(0, 1).toUpperCase() + word.substring(1, word.length()) + space;
		}
		return writing;
	}

	public String[] mysplit(String tokenizer, String deli) {
		StringTokenizer stk = new StringTokenizer(tokenizer, deli);
		String[] tokenString = new String[stk.countTokens()];
		for (int i = 0; i < tokenString.length; i++) {
			tokenString[i] = stk.nextToken().trim();
		}
		return tokenString;
	}

	/**
	 * @description 숫자형 콤마 찍기
	 */
	public static String toNumaricFormat(int var) {
		return String.format("%,d", var);
	}

	/**
	 * @description 숫자형 콤마 찍기
	 */
	public static String toNumaricFormat(String var) {
		int i = Integer.parseInt(nvl(var, "0"));
		return String.format("%,d", i);
	}

	public static String StringRolling(String string, int length) {
		if (string.length() > length) {
			return string.substring(0, length) + "...";
		} else {
			return string;
		}
	}

	public static String getDateYY(String text) {
		text = nvl(text);

		if (!"".equals(text)) {
			if (4 <= text.length()) {
				text = text.substring(0, 4);
			}
		}
		return text;
	}

	/**
	 * valid 한 year 만 리턴한다.<br>
	 * 경력 등의 년도 필드로 사용되므로, 1900 ~ 2100 년 까지만 유효하다고 가정한다.
	 * 
	 * @param text
	 * @return
	 */
	public static String getDateValidYY(String text) {
		if (text == null || "".equals(text.trim()))
			return "";
		text = text.trim();
		try {
			Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return "";
		}

		if (text.length() < 4) {
			return "";
		} else {
			text = text.substring(0, 4);
			try {
				int year = Integer.parseInt(text);
				if (year < 1900 || year > 2100)
					return "";
			} catch (NumberFormatException e) {
				return "";
			}
		}
		return text;
	}

	public static String StrSecurity(String value) {
		String val = "";
		String kpiGaolValueOriginal = value;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < kpiGaolValueOriginal.length(); i++) {
			if (i == 0) {
				sb.append(kpiGaolValueOriginal.charAt(i));
			} else {
				sb.append("*");
			}
		}
		val = sb.toString();
		// System.out.println("val=>" + val);
		return val;
	}

	public static String DateToDot2(String str, String dechar) {
		String returnstr = "";

		if (str == null || str.trim().length() < 4)
			return returnstr;

		str = str.trim();
		int len = str.length();

		switch (len) {
			case 5:
				return str.substring(0, 4) + dechar + "0" + str.substring(4, 5);
			case 6:
				return str.substring(0, 4) + dechar + str.substring(4, 6);
			case 7:
				return str.substring(0, 4) + dechar + "0" + str.substring(4, 5) + dechar + str.substring(5, 7);
			case 8:
				return str.substring(0, 4) + dechar + str.substring(4, 6) + dechar + str.substring(6, 8);
		}
		return str;
	}

	/**
	 * 년도만 추출한다.
	 * 
	 * @param str
	 * @param dechar
	 * @return
	 */
	public static String extractYear(String str) {
		if (str == null)
			return "";
		str = str.trim();
		if (str.length() <= 4)
			return str;
		return str.substring(0, 4);
	}

	/**
	 * removeWhitepaceAndSpChar 와 동일하지만, 연속적으로 나오는 공백은 1개로 축약하고, 마지막의 공백은 제거한다.
	 * 
	 * @param s
	 * @param removeBrace true이면 그냥 제거, false이면 delim로 치환
	 * @return
	 */
	public static String replaceWhitepaceAndRemoveSpChar(String s, boolean removeBrace) {
		return replaceWhitepaceAndRemoveSpChar(s, removeBrace, '_');
	}

	public static String replaceWhitepaceAndRemoveSpChar(String s, boolean removeBrace, char delim) {
		s = s.trim();
		if ("".equals(s))
			return "";
		char[] ch = s.toCharArray();
		StringBuilder b = new StringBuilder();
		boolean prevEmpty = false;
		for (int i = 0; i < ch.length; i++) {
			// if(isKorean(ch[i]) || Character.isDigit( ch[i] )) {
			if (Character.isLetterOrDigit(ch[i])) {
				b.append(ch[i]);
				prevEmpty = false;
			} else if (!prevEmpty && Character.isWhitespace(ch[i])) {
				b.append(delim);
				prevEmpty = true;
			} else if (!removeBrace && (ch[i] == '(' || ch[i] == ')')) {
				b.append(delim);
				prevEmpty = true;
			}
		}
		s = b.toString();
		return s;
	}

	/**
	 * 문자, 가로, 공백, 숫자 문자만 남기고 나머지는 삭제함.
	 * 
	 * @param s
	 * @return
	 */
	public static String removeSpCharWithBrace(String s) {
		if (s == null) {
			return null;
		} else {
			s = s.trim();
			char[] ch = s.toCharArray();
			StringBuilder b = new StringBuilder();
			boolean prevEmpty = false;
			for (int i = 0; i < ch.length; i++) {
				if (Character.isLetterOrDigit(ch[i])) {
					b.append(ch[i]);
					prevEmpty = false;
				} else if (!prevEmpty && Character.isWhitespace(ch[i])) {
					b.append(' ');
					prevEmpty = true;
				} else if (ch[i] == '(' || ch[i] == ')') {
					b.append(ch[i]);
					prevEmpty = false;
				}
			}
			s = b.toString();
			return s;
		}
	}

	/**
	 * 가로, 공백, 숫자 문자만 남기고 나머지는 삭제함.
	 * 
	 * @param s
	 * @return
	 */
	public static String removeSpCharTrim(String s) {
		char[] ch = s.toCharArray();
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < ch.length; i++) {
			if (Character.isLetterOrDigit(ch[i])) {
				b.append(ch[i]);
			}
		}
		s = b.toString();
		return s;
	}

	public static String removeBrace(String val) {
		if (val == null) {
			return null;
		} else {
			String newStr = val;
			int sp = -1;
			int ep = -1;
			if ((sp = val.indexOf("(")) >= 0 && (ep = val.indexOf(")", sp)) > 0) {
				newStr = val.substring(0, sp);
				if (ep + 1 < val.length()) {
					try {
						newStr += val.substring(ep + 1, val.length());
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
				return removeBrace(newStr);
				// } else if((sp = val.indexOf("(")) > 0) {
				// newStr = val.substring(0, sp);
			}
			return newStr;
		}
	}

	public static String removeBigBrace(String val) {
		if (val == null) {
			return null;
		} else {
			String newStr = val;
			int sp = -1;
			int ep = -1;
			if ((sp = val.indexOf("[")) >= 0 && (ep = val.indexOf("]", sp)) > 0) {
				newStr = val.substring(0, sp);
				if (ep + 1 < val.length()) {
					try {
						newStr += val.substring(ep + 1, val.length());
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
				return removeBigBrace(newStr);
				// } else if((sp = val.indexOf("(")) > 0) {
				// newStr = val.substring(0, sp);
			}
			return newStr;
		}
	}

	public static String removeSpChar(String s) {
		char[] ch = s.toCharArray();
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < ch.length; i++) {
			if (Character.isLetterOrDigit(ch[i])) {
				b.append(ch[i]);
			}
		}
		s = b.toString();
		return s;
	}

	public static String remainDigit(String s) {
		char[] ch = s.toCharArray();
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < ch.length; i++) {
			if (Character.isDigit(ch[i])) {
				b.append(ch[i]);
			}
		}
		s = b.toString();
		return s;
	}

	public static String removeWhitepace(String s) {
		char[] ch = s.toCharArray();
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < ch.length; i++) {
			if (!Character.isWhitespace(ch[i])) {
				b.append(ch[i]);
			}
		}
		s = b.toString();
		return s;
	}

	public static final boolean isEmptyTrimmed(String s) {
		return (s == null || s.trim().length() == 0);
	}

	public static void format(StringBuilder trans, String[] source, String everyLeft, String everyRight, String left,
			String separator, String right) {
		trans.append(left);
		if (source.length > 0) {
			trans.append(everyLeft);
			trans.append(source[0]);
			trans.append(everyRight);
		}
		for (int i = 1; i < source.length; i++) {
			trans.append(separator);
			trans.append(everyLeft);
			trans.append(source[i]);
			trans.append(everyRight);
		}
		trans.append(right);
	}

	public static void format(StringBuffer trans, String[] source, String everyLeft, String everyRight, String left,
			String separator, String right) {
		trans.append(left);
		if (source.length > 0) {
			trans.append(everyLeft);
			trans.append(source[0]);
			trans.append(everyRight);
		}
		for (int i = 1; i < source.length; i++) {
			trans.append(separator);
			trans.append(everyLeft);
			trans.append(source[i]);
			trans.append(everyRight);
		}
		trans.append(right);
	}

	public static void format(StringBuilder trans, String[] source, String left, String separator, String right) {
		trans.append(left);
		if (source.length > 0) {
			trans.append(source[0]);
		}
		for (int i = 1; i < source.length; i++) {
			trans.append(separator);
			trans.append(source[i]);
		}
		trans.append(right);
	}

	public static String format(String[] source, String left, String separator, String right) {
		StringBuilder trans = new StringBuilder(16);
		format(trans, source, left, separator, right);
		return trans.toString();
	}

	public static void format(StringBuffer trans, String[] source, String left, String separator, String right) {
		trans.append(left);
		if (source.length > 0) {
			trans.append(source[0]);
		}
		for (int i = 1; i < source.length; i++) {
			trans.append(separator);
			trans.append(source[i]);
		}
		trans.append(right);
	}

	public static String format(String[] source, String separator) {
		return format(source, "", separator, "");
	}

	public static int hashCode(String[] source) {
		if (source == null) {
			return 0;
		}
		int h = 0;
		for (int i = 0; i < source.length; i++) {
			h ^= source[i] == null ? 0 : source[i].hashCode();
		}
		return h;
	}

	/*
	 * 키워드의 첫자가 한글인지의 여부
	 */
	public static boolean isKoreanValid(String keyword) {
		char[] chArr = keyword.toCharArray();
		int intCh = Character.getType(chArr[0]);

		boolean iskorean = false;

		if (intCh == 5) {
			iskorean = true;
		}

		return iskorean;
	}

	/*
	 * 키워드의 첫자가 한글, 영어인지의 여부
	 */
	public static boolean isKoreanEnglishValid(String keyword) {
		char[] chArr = keyword.toCharArray();
		int intCh = Character.getType(chArr[0]);

		boolean iskoreanEnglish = false;

		if (intCh == 1 || intCh == 2 || intCh == 5) {
			iskoreanEnglish = true;
		}

		return iskoreanEnglish;
	}

	public static boolean isAllNumeric(String args) {
		char[] chArr = args.toCharArray();
		for (int i = 0; i < chArr.length; i++) {
			int type = Character.getType(chArr[i]);
			if (type != 9)
				return false;
		}

		return true;
	}

	/*
	 * 글자 공백제거(문자열)
	 */
	public static String doBlankRemove(String keyword) {
		return keyword.replaceAll("\\p{Space}", "");
	}

	/*
	 * 글자 공백제거(배열)
	 */
	public static String[] doBlankRemove(String[] keyword) {
		if (keyword != null && keyword.length > 0) {
			for (int i = 0; i < keyword.length; i++) {
				keyword[i] = keyword[i].replaceAll("\\p{Space}", "");
			}
		}
		return keyword;
	}

	public static String voidNull(String str) {
		if (str == null) {
			return "";
		}
		if (str.trim().equals("")) {
			return "";
		} else {
			return str.trim();
		}
	}

	public static boolean isEmpty(String str) {
		return (str == null || str.trim().equals(""));
	}

	public static void xmlStartBuffer(StringBuilder buffer, String successSearch) {
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		buffer.append(
				"<ServiceResult xsi:schemaLocation=\"http://rndnavi.kisti.re.kr/rndnavi/ServiceResult-schema ServiceResult.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.kisti.re.kr/rndnavi/ServiceResult-schema\">\n");
		buffer.append("	<Info>\n");
		buffer.append("		<Api_Name>" + successSearch + "</Api_Name>\n");
		buffer.append("		<Description><![CDATA[API에 대한 상세 설명 입니다.]]></Description>\n");
		buffer.append("	</Info>\n");
	}

	public static void xmlNormalBuffer(StringBuilder buffer) {
		buffer.append("	<ResultData>\n");
	}

	public static void xmlPageBuffer(StringBuilder buffer, int tSize, int pSize, int pNum) {
		buffer.append("	<Param>\n");
		buffer.append("		<TotalSize>" + tSize + "</TotalSize>\n");
		buffer.append("		<PageSize>" + pSize + "</PageSize>\n");
		buffer.append("		<PageNum>" + pNum + "</PageNum>\n");
		buffer.append("	</Param>\n");
		buffer.append("	<ResultData>\n");
	}

	public static void xmlEndBuffer(StringBuilder buffer) {
		buffer.append("	</ResultData>\n");
		buffer.append("</ServiceResult>\n");
	}

	public static String xmlKeyArrayToString(String[] inputArrayString, String delimiter) {
		String resultString = "";

		if (inputArrayString != null && inputArrayString.length > 0) {
			for (int i = 0; i < inputArrayString.length; i++) {
				resultString += inputArrayString[i] + delimiter;
			}
		}

		return resultString;
	}

	public static String[] xmlKeyStringToArray(String inputString, String delimiter) {
		String[] resultArray = null;

		StringTokenizer st = new StringTokenizer(inputString, delimiter);
		if (st.countTokens() > 0) {
			resultArray = new String[st.countTokens()];

			for (int i = 0; st.hasMoreTokens(); i++) {
				resultArray[i] = st.nextToken();
			}
		}

		return resultArray;
	}

	private static final DecimalFormat df = new DecimalFormat("#,###.####");

	public static String cvtNumber(String str) {
		String retStr;

		try {
			java.math.BigDecimal dcmData = new java.math.BigDecimal(str);
			retStr = df.format(dcmData);
		} catch (NumberFormatException nfe) {
			retStr = str;
		}
		return retStr;
	}

	public static String formatComma(String s) {
		if (isNull(s))
			return s;
		if (!isNumber(s))
			return s;
		char c = '0';
		int len = s.length();
		StringBuilder sb = new StringBuilder();

		int front = len % 3;
		for (int i = 0; i < len; i++) {
			c = s.charAt(i);

			if (i < front) {
				sb.append(c);
			} else if ((len - i) % 3 == 0) {
				if (i != 0)
					sb.append(",");
				sb.append(c);
			} else {
				sb.append(c);
			}
		}

		return sb.toString();
	}

	public static String makeOnlyNumber(String s) {
		char c = '0';
		if (isNull(s))
			return s;
		StringBuilder sb = new StringBuilder();
		int len = s.length();
		for (int i = 0; i < len; i++) {
			c = s.charAt(i);
			if ((c < '0') || (c > '9')) {
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String fromdb(String str) {
		String tmpstr = new String();
		try {
			String s = ksc2uni(str);
			return s;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(System.err);
		}
		return tmpstr;
	}

	public static String ksc2uni(String kscstr) throws UnsupportedEncodingException {
		if (kscstr == null) {
			return " ";
		} else {
			return new String(kscstr.getBytes("KSC5601"), "8859_1");
		}
	}

	public static String ms2euc(String kscstr) throws UnsupportedEncodingException {
		if (kscstr == null) {
			return " ";
		} else {
			return new String(kscstr.getBytes("MS949"), "euc-kr");
		}
	}

	public static String euc2ms(String kscstr) throws UnsupportedEncodingException {
		if (kscstr == null) {
			return " ";
		} else {
			return new String(kscstr.getBytes("euc-kr"), "MS949");
		}
	}

	public static String todb(String str) {
		String tmpstr = new String();
		try {
			tmpstr = uni2ksc(str);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(System.err);
		}
		return tmpstr;
	}

	public static String uni2ksc(String unicodestr) throws UnsupportedEncodingException {
		if (unicodestr == null) {
			return " ";
		} else {
			return new String(unicodestr.getBytes("8859_1"), "KSC5601");
		}
	}

	public static String utf2uni(String kscstr) throws UnsupportedEncodingException {
		if (kscstr == null) {
			return " ";
		} else {
			return new String(kscstr.getBytes("UTF-8"), "8859_1");
		}
	}

	public static String uni2utf(String unicodestr) throws UnsupportedEncodingException {
		if (unicodestr == null) {
			return " ";
		} else {
			return new String(unicodestr.getBytes("8859_1"), "UTF-8");
		}
	}

	public static String converthtml(String _text) {
		StringBuffer html = new StringBuffer();
		String text = _text;

		int startindex = 0;
		int endindex = 0;

		while ((endindex = text.indexOf("\n", startindex)) > -1) {
			html.append(text.substring(startindex, endindex));
			html.append("<br>");
			startindex = endindex + 1;
		}
		html.append(text.substring(startindex, text.length()));
		return html.toString();
	}

	// 문장을 HTML용으로 변환.
	public static String settag(String s) {
		int j = s.length();

		StringBuffer stringbuffer = new StringBuffer(j + 500);

		for (int i = 0; i < j; i++) {
			if (s.charAt(i) == '<') {
				stringbuffer.append("&lt");
			} else if (s.charAt(i) == '>') {
				stringbuffer.append("&gt");
			} else {
				stringbuffer.append(s.charAt(i));
			}
		}

		return stringbuffer.toString();
	}

	// HTML 사용 안 함.
	public static String setbr(String s) {
		int j = s.length();

		StringBuffer stringbuffer = new StringBuffer(j + 500);

		for (int i = 0; i < j; i++) {
			if (s.charAt(i) == '\n') {
				stringbuffer.append("<br>");
			} else if (s.charAt(i) == '\r') {
				stringbuffer.append("");
			} else {
				stringbuffer.append(s.charAt(i));
			}
		}
		return stringbuffer.toString();
	}

	// html사용 안 함.(공백으로 변환.)
	public static String setspace(String s) {
		int j = s.length();

		StringBuffer stringbuffer = new StringBuffer(j + 500);

		for (int i = 0; i < j; i++) {
			if (s.charAt(i) == '\n') {
				stringbuffer.append("\\n");
			} else if (s.charAt(i) == '\r') {
				stringbuffer.append("\\r");
			} else {
				stringbuffer.append(s.charAt(i));
			}
		}
		return stringbuffer.toString();
	}

	public static String setspc(String _text) {
		StringBuffer html = new StringBuffer();
		String text = _text;

		int startindex = 0;
		int endindex = 0;

		while ((endindex = text.indexOf(" ", startindex)) > -1) {
			html.append(text.substring(startindex, endindex));
			html.append("&nbsp;");
			startindex = endindex + 1;
		}
		html.append(text.substring(startindex, text.length()));
		return html.toString();
	}

	// encoding
	public static String a2k(String str) {
		String rtn = null;
		try {
			rtn = (str == null) ? "" : new String(str.getBytes("8859_1"), "euc-kr");
		} catch (java.io.UnsupportedEncodingException e) {
		}
		return rtn;
	}

	// encoding
	public static String k2a(String str) {
		String rtn = null;
		try {
			rtn = (str == null) ? "" : new String(str.getBytes("euc-kr"), "8859_1");
		} catch (java.io.UnsupportedEncodingException e) {
		}
		return rtn;
	}

	// SCRIPT에서 FORM필드의 VALUE값 SETTING시 사용.
	static public String getscripttag(String str) {
		if (str != null) {
			str = str.trim();
			str = replace(str, "\n", "\\n");
			str = replace(str, "\r", "");
			str = replace(str, "`|", "\\n");
			str = replace(str, "'", "\\'");
			str = replace(str, "\"", "\\\"");
			str = replace(str, "(", "\\(");
			str = replace(str, ")", "\\)");
			str = replace(str, "/", "\\/");
			str = replace(str, ",", "\\,");
			str = replace(str, "=", "\\=");
			str = replace(str, "-", "\\-");
			str = replace(str, "&", "\\&");
			str = replace(str, "#", "\\#");
			str = replace(str, "^", "\\^");
			str = replace(str, ";", "\\;");
			str = replace(str, ":", "\\:");
			str = replace(str, "*", "\\*");
			str = replace(str, "<", "\\<");
			str = replace(str, ">", "\\>");
		}
		return str;
	}

	// TEXTFIELD에 VALUE값 SETTING시 사용
	public static String gettexttag(String str) {
		if (str != null) {
			str = str.trim();
			str = replace(str, "\"", "&quot;");
			str = replace(str, "'", "&#39;");
		}
		return str;
	}

	// 문자를 INT형으로 치환, 값이 NULL일 경우는 VAL로 돌려준다.
	public static int stoi(String str, int val) {
		if (str == null) {
			return val;
		}
		if (str.equals("")) {
			return val;
		} else {
			return Integer.valueOf(str).intValue();
		}
	}

	public static String removechar(String value, char remove_char) {
		int flag = 0;
		String newval = "";
		int index = 0;
		int temp = 0;
		flag = value.indexOf(remove_char, 0);
		if (flag == -1)
			return value.trim();
		do {
			index = value.indexOf(remove_char, temp);
			if (index == -1) {
				newval = newval.concat(value.substring(temp));
				break;
			}
			newval = newval.concat(value.substring(temp, index));
			temp = index + 1;
		} while (true);
		return newval.trim();
	}

	public static String nullcheck(String original_val) {
		if (original_val == null) {
			return "";
		} else {
			return original_val;
		}
	}

	public static String nullcheck(String original_val, String replace_val) {
		if (original_val == null || original_val.equals("null") || "".equals(original_val)) {
			return replace_val;
		} else {
			return original_val;
		}
	}

	public static String nullcheck(String original_val, int replace_val) {
		if (original_val == null) {
			return replace_val + "";
		} else {
			return original_val;
		}
	}

	public static String nullcheck(Object original_val) {
		if (original_val == null) {
			return "";
		} else {
			return original_val.toString();
		}
	}

	public static void setMapDateFormat(Map<String, Object> map, String dateKey, String formatString) {
		Object createDate = map.get(dateKey);
		DateFormat fm = new SimpleDateFormat(formatString);
		String datestr = fm.format(createDate);
		map.put(dateKey, datestr);
	}

	/**
	 * 마지막 으로 처음나온 문자를 변환
	 * 
	 * @param text
	 * @param regex
	 * @param replacement
	 * @return
	 */
	public static String replaceLast(String text, String regex, String replacement) {
		return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
	}

//	public static void main(String[] args) {
//		System.out.println(replaceLast("aa/aa/aa", "aa", "cc"));
//	}
}
