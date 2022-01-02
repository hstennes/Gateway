package com.logic.engine;

import com.logic.components.CompType;

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

	public static BiFunction<Integer[], BiFunction<Integer, Integer, Integer>, Integer> basicLogic;
	
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
		twoInput.add((a, b) -> ~(a & b));
		twoInput.add((a, b) -> ~(a | b));
		twoInput.add((a, b) -> ~(a ^ b));
	}

	public static int basicLogic(int[] inputs, int function){
		int output = inputs[0];
		for(int i = 1; i < inputs.length; i++){
			output = twoInput.get(function).apply(output, inputs[i]);
		}
		return output;
	}

	public static int getFunctionIndex(CompType type){
		switch(type){
			case AND:
				return 0;
			case NAND:
				return 3;
			case OR:
				return 1;
			case NOR:
				return 4;
			case XOR:
				return 2;
			case XNOR:
				return 5;
			default:
				throw new IllegalArgumentException("getFunctionIndex requires BasicGate type");
		}
	}
}
