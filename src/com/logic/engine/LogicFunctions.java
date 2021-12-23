package com.logic.engine;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Defines all Functions that can be used by LComponents in their update() methods. 
 * @author Hank Stennes
 *
 */
public class LogicFunctions {

	/**
	 * The list of all functions that accept one input. See setFunctions() for info.
	 */
	public static ArrayList<Function<Integer, Integer>> singleInput = new ArrayList<>();

	/**
	 * The list of all functions that accept two inputs. See setFunctions() for info.
	 */
	public static ArrayList<BiFunction<Integer, Integer, Integer>> twoInput = new ArrayList<>();
	
	/**
	 * Initializes all logic functions. Single input functions are loaded in the order NOT, BUFFER, and multi-input functions are loaded in 
	 * the order AND, NAND, OR, NOR, XOR, XNOR. This is the order that the functions appear in their respective arrays.
	 */
	public static void setFunctions() {
		singleInput.add(a -> ~a);
		singleInput.add(a -> a);
		twoInput.add((a, b) -> a & b);
		twoInput.add((a, b) -> a | b);
		twoInput.add((a, b) -> a ^ b);
	}
}
