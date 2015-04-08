package org.ken.brainfcuk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;

import javax.annotation.processing.FilerException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.ken.brainfcuk.opcodes.OpCode;
import org.ken.brainfcuk.opcodes.OpCodeEnum;

public class BrainFcuk2Java {

	private static boolean verbose = false;
	private static boolean debug = false;
	private static boolean hasReadIn = false;

	public static void main(String[] args) {

		boolean createJavaSource = false;

		Options options = new Options();

		options.addOption("v", false, "be verbose");
		options.addOption("S", false, "output java source code");
		options.addOption("D", false, "prints debug information");

		if (args.length == 0) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("BrainFcuk2Java [source]", options);
			System.exit(1);
		}

		CommandLineParser parser = new BasicParser();
		CommandLine line = null;
		try {
			line = parser.parse(options, args);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}

		if (line.hasOption("v")) {
			verbose = true;
		}
		if (line.hasOption("S")) {
			createJavaSource = true;
		}
		if(line.hasOption("D")) {
			debug = true;
		}

		BrainFcuk2Java bf2j = new BrainFcuk2Java();
		bf2j.beginProcess((String)line.getArgList().get(0), createJavaSource);
		if(verbose) {
			System.out.println("Finished!");
		}
	}

	public void beginProcess(String fileName, boolean createJavaSource) {
		File bfFile;
		// Open the file
		try {
			bfFile = openSourceFile(fileName);
		} catch (FileNotFoundException | FilerException e) {
			e.printStackTrace();
			return;
		}
		String source;
		try {
			source = readInFile(bfFile);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		BrainFcuk2JavaObj obj = processBrainFcukCode(source);
		int lastSlash = -1;
		if (fileName.contains("\\")) {
			lastSlash = fileName.lastIndexOf("\\");
			obj.dir = fileName.substring(0, lastSlash).replace("\\", "\\\\");
		}
		obj.className = fileName
				.substring(lastSlash + 1, fileName.indexOf("."));
		obj.createJavaSource = createJavaSource;
		obj.verbose = verbose;
		obj.hasReadIn = hasReadIn;
		ClassBuilder cb = new ClassBuilder();
		cb.buildClass(obj);
	}

	/**
	 * Creates a stack of op code objects that will form the BrainFuck program.
	 * 
	 * @param source
	 *            BrainFuck program source
	 * @return Stack of op code objects
	 */
	private BrainFcuk2JavaObj processBrainFcukCode(String source) {
		if (verbose) {
			System.out.println("Begin Processing BrainFuck Program");
		}
		long startTime = System.currentTimeMillis();
		int ptr = 0;
		int pc = 0;
		int maxPtr = 0;

		Stack<OpCode> opCodes = new Stack<OpCode>();
		if(debug) {
			System.out.println(source);
		}
		while (pc != source.length()) {
			String opcode = source.substring(pc, pc + 1);
			OpCode last = null;
			if (!opCodes.isEmpty()) {
				last = opCodes.pop();
			}
			switch (opcode) {
			case ">":
				ptr++;
				if (maxPtr < ptr) {
					maxPtr = ptr;
				}
				if (last != null && last.type == OpCodeEnum.PTRUP) {
					last.value++;
					opCodes.push(last);
				} else {
					if (last != null) {
						opCodes.push(last);
					}
					opCodes.push(new OpCode(OpCodeEnum.PTRUP, 1));
				}
				break;
			case "<":
				ptr--;
				if (last != null && last.type == OpCodeEnum.PTRDOWN) {
					last.value--;
					opCodes.push(last);
				} else {
					if (last != null) {
						opCodes.push(last);
					}
					opCodes.push(new OpCode(OpCodeEnum.PTRDOWN, -1));
				}
				break;
			case "+":
				if (last != null && last.type == OpCodeEnum.VALUEUP) {
					last.value++;
					opCodes.push(last);
				} else {
					if (last != null) {
						opCodes.push(last);
					}
					opCodes.push(new OpCode(OpCodeEnum.VALUEUP, 1));
				}
				break;
			case "-":
				if (last != null && last.type == OpCodeEnum.VALUEDOWN) {
					last.value--;
					opCodes.push(last);
				} else {
					if (last != null) {
						opCodes.push(last);
					}
					opCodes.push(new OpCode(OpCodeEnum.VALUEDOWN, -1));
				}
				break;
			case ".":
				if (last != null) {
					opCodes.push(last);
				}
				opCodes.push(new OpCode(OpCodeEnum.READOUT));
				break;
			case ",":
				hasReadIn = true;
				if (last != null) {
					opCodes.push(last);
				}
				opCodes.push(new OpCode(OpCodeEnum.READIN));
				break;
			case "[":
				if (last != null) {
					opCodes.push(last);
				}
				opCodes.push(new OpCode(OpCodeEnum.BEGINLOOP));
				break;
			case "]":
				if (last != null) {
					opCodes.push(last);
				}
				opCodes.push(new OpCode(OpCodeEnum.ENDLOOP));
				break;
			default:
				opCodes.push(last);
				break;
			}
			pc++;
		}
		if(debug) {
			System.out.println("PC: " + pc);
			System.out.println("Stack Size: " + opCodes.size());
		}
		BrainFcuk2JavaObj obj = new BrainFcuk2JavaObj();
		obj.opCodes = opCodes;
		obj.maxPtr = maxPtr;
		if (verbose) {
			System.out.println("Finished processing Brainfuck program in "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}
		return obj;
	}

	/**
	 * Reads in the file to a String Buffer
	 * 
	 * @param bfFile
	 *            File to read
	 * @return File contents
	 * @throws IOException
	 */
	private String readInFile(File bfFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(bfFile));
		StringBuilder sb = new StringBuilder();
		String line;
		line = br.readLine();
		while (line != null) {
			// Remove New Line characters
			line = line.replace("\r", "").replace("\n", "");
			sb.append(line);
			line = br.readLine();
		}
		br.close();
		return sb.toString();
	}

	/**
	 * Attempts to open the file and return the file object
	 * 
	 * @param fileName
	 *            File to open
	 * @return Opened file object
	 * @throws FileNotFoundException
	 * @throws FilerException
	 *             File cannot be read
	 */
	private File openSourceFile(String fileName) throws FileNotFoundException,
			FilerException {
		File file = new File(fileName);
		if (!file.exists()) {
			throw new FileNotFoundException("File " + fileName
					+ " was not found.");
		}
		if (!file.canRead()) {
			throw new FilerException("Unable to read file " + fileName);
		}
		return file;
	}

}
