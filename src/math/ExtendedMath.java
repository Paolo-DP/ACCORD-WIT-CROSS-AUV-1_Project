package math;

/**
 * A class that provides additional math functions aside from Java's math.
 * @see Math
 * @author Ocampo
 */
public final class ExtendedMath {
	/**
	 * Returns the quotient of {@code a} and {@code b}.
	 * @param a Dividend.
	 * @param b Divisor.
	 * @return Quotient of {@code a} and {@code b}.
	 */
	public static double div(double a, double b) {
		double absA = Math.abs(a);
		double absB = Math.abs(b);
		double quotientSign = Math.signum(a) / Math.signum(b);
		return quotientSign * Math.floor(absA / absB);
	}
	
	/**
	 * Returns the quotient of {@code a} and {@code b}.
	 * @param a Dividend.
	 * @param b Divisor.
	 * @return Quotient of {@code a} and {@code b}.
	 */
	public static float div(float a, float b) {
		float absA = Math.abs(a);
		float absB = Math.abs(b);
		float quotientSign = Math.signum(a) / Math.signum(b);
		return quotientSign * (float) Math.floor(absA / absB);
	}
	
	/**
	 * Returns the remainder of {@code a/b}. The remainder is positive regardless 
	 * of the signs of {@code a} and {@code b}.
	 * @param a Dividend.
	 * @param b Divisor.
	 * @return Remainder of {@code a/b}.
	 */
	public static double rem(double a, double b) {
		double absA = Math.abs(a);
		double absB = Math.abs(b);
		double absQ = Math.floor(absA / absB);
		return absA - absB * absQ;
	}
	
	/**
	 * Returns the remainder of {@code a/b}. The remainder is positive regardless 
	 * of the signs of {@code a} and {@code b}.
	 * @param a Dividend.
	 * @param b Divisor.
	 * @return Remainder of {@code a/b}.
	 */
	public static float rem(float a, float b) {
		float absA = Math.abs(a);
		float absB = Math.abs(b);
		float absQ = (float) Math.floor(absA / absB);
		return absA - absB * absQ;
	}
	
	/**
	 * Returns the minimum value if the input is less than it or the maximum if the input 
	 * is more than it. Otherwise it returns the same number. That is returns {@code min} 
	 * if {@code x < min}, {@code max} if {@code x > max}, otherwise {@code x}.
	 * @param x Input
	 * @param min Minimum value.
	 * @param max Maximum value.
	 * @return {@code min} if {@code x < min}, {@code max} if {@code x > max}, 
	 *		   otherwise {@code x}
	 */
	public static double bound(double x, double min, double max) {
		if (x < min) return min;
		if (x > max) return max;
		return x;
	}
	
	/**
	 * Returns the minimum value if the input is less than it or the maximum if the input 
	 * is more than it. Otherwise it returns the same number. That is returns {@code min} 
	 * if {@code x < min}, {@code max} if {@code x > max}, otherwise {@code x}.
	 * @param x Input
	 * @param min Minimum value.
	 * @param max Maximum value.
	 * @return {@code min} if {@code x < min}, {@code max} if {@code x > max}, 
	 *		   otherwise {@code x}
	 */
	public static float bound(float x, float min, float max) {
		if (x < min) return min;
		if (x > max) return max;
		return x;
	}
	
	/**
	 * Returns the minimum value if the input is less than it or the maximum if the input 
	 * is more than it. Otherwise it returns the same number. That is returns {@code min} 
	 * if {@code x < min}, {@code max} if {@code x > max}, otherwise {@code x}.
	 * @param x Input
	 * @param min Minimum value.
	 * @param max Maximum value.
	 * @return {@code min} if {@code x < min}, {@code max} if {@code x > max}, 
	 *		   otherwise {@code x}
	 */
	public static int bound(int x, int min, int max) {
		if (x < min) return min;
		if (x > max) return max;
		return x;
	}
	
	/**
	 * Returns the minimum value if the input is less than it or the maximum if the input 
	 * is more than it. Otherwise it returns the same number. That is returns {@code min} 
	 * if {@code x < min}, {@code max} if {@code x > max}, otherwise {@code x}.
	 * @param x Input
	 * @param min Minimum value.
	 * @param max Maximum value.
	 * @return {@code min} if {@code x < min}, {@code max} if {@code x > max}, 
	 *		   otherwise {@code x}
	 */
	public static long bound(long x, long min, long max) {
		if (x < min) return min;
		if (x > max) return max;
		return x;
	}
	
	/**
	 * Returns the modulus of a and b, that is, {@code a mod b}
	 * @param a Dividend input.
	 * @param b Divisor input.
	 * @return {@code a mod b}
	 */
	public static int mod(int a, int b) {
		return (a % b + b) % b;
	}
	
	/**
	 * Returns the modulus of a and b, that is, {@code a mod b}
	 * @param a Dividend input.
	 * @param b Divisor input.
	 * @return {@code a mod b}
	 */
	public static long mod(long a, long b) {
		return (a % b + b) % b;
	}
	
	/**
	 * Returns the modulus of a and b, that is, {@code a mod b}
	 * @param a Dividend input.
	 * @param b Divisor input.
	 * @return {@code a mod b}
	 */
	public static double mod(double a, double b) {
		double x = Math.signum(a) * rem(a, b) + b;
		return Math.signum(x) * rem(x, b);
	}
	
	/**
	 * Returns the modulus of a and b, that is, {@code a mod b}
	 * @param a Dividend input.
	 * @param b Divisor input.
	 * @return {@code a mod b}
	 */
	public static float mod(float a, float b) {
		float x = Math.signum(a) * rem(a, b) + b;
		return Math.signum(x) * rem(x, b);
	}
}
