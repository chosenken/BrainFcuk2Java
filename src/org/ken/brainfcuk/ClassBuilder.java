package org.ken.brainfcuk;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

import org.ken.brainfcuk.opcodes.OpCode;

public class ClassBuilder {

	public static final String nl = "\r\n";
	public static final String tab = "    ";
	private static boolean verbose = false;

	public void buildClass(BrainFcuk2JavaObj obj) {
		verbose = obj.verbose;
		ArrayList<OpCode> opCodes = new ArrayList<OpCode>(obj.opCodes);
		String source = createJavaSrcFromOpCodes(opCodes, obj.maxPtr,
				obj.hasReadIn);
		if (obj.createJavaSource) {
			saveSourceFile(source, obj.dir, obj.className);
		}
		makeClass(source, obj.dir, obj.className);
	}

	private void saveSourceFile(String source, String dir, String className) {
		if (verbose) {
			System.out.println("Creating Java Source file");
		}
		StringBuilder sb = new StringBuilder();
		sb.append("public class " + className + " {").append(nl).append(source)
				.append(nl).append("}").append(nl);
		if (dir == null || dir.isEmpty()) {
			dir = ".";
		}
		File directory = new File(dir);
		File sourceFile = new File(directory, className + ".java");
		BufferedWriter br = null;
		try {
			br = new BufferedWriter(new FileWriter(sourceFile));
			br.write(sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (verbose) {
			System.out.println("Java Source file created");
		}
	}

	private void makeClass(String source, String dir, String className) {
		ClassPool pool = ClassPool.getDefault();
		CtClass cc = pool.makeClass(className);
		CtMethod m;
		try {
			m = CtNewMethod.make(source, cc);
			cc.addMethod(m);
			if (dir == null) {
				dir = ".";
			}
			cc.writeFile(dir);
		} catch (CannotCompileException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String createJavaSrcFromOpCodes(ArrayList<OpCode> opCodes,
			int maxPtr, boolean hasReadIn) {
		if (verbose) {
			System.out.println("Started Java Source Construction");
		}
		long startTime = System.currentTimeMillis();
		int tabs = 1;
		StringBuilder sb = new StringBuilder();
		addLine(sb,
				"public static void main (String[] args) throws Exception {",
				tabs);
		tabs++;
		addLine(sb, "byte[] buffer = new byte[30000];", tabs);
		addLine(sb, "int ptr = 0;", tabs);
		if (hasReadIn) {
			addLine(sb,
					"java.io.InputStreamReader br = new java.io.InputStreamReader(System.in);",
					tabs);
			addLine(sb, "char ch;", tabs);
		}

		for (OpCode opCode : opCodes) {
			switch (opCode.type) {
			case PTRUP:
			case PTRDOWN:
				addLine(sb, "ptr = ptr + " + opCode.value + ";", tabs);
				break;
			case VALUEUP:
			case VALUEDOWN:
				addLine(sb, "buffer[ptr] = (byte) (buffer[ptr] + "
						+ opCode.value + ");", tabs);
				break;
			case READOUT:
				addLine(sb, "System.out.print((char)buffer[ptr]);", tabs);
				break;
			case READIN:
				addLine(sb, "ch = (char)br.read();", tabs);
				addLine(sb, "buffer[ptr] = (byte)ch;", tabs);
				break;
			case BEGINLOOP:
				addLine(sb, "while(buffer[ptr] != 0) { ", tabs);
				tabs++;
				break;
			case ENDLOOP:
				tabs--;
				addLine(sb, "}", tabs);
			default:
				break;
			}
		}
		if (hasReadIn) {
			addLine(sb, "br.close();", tabs);
		}
		tabs--;
		addLine(sb, "}", tabs);
		if (verbose) {
			System.out.println("Finished Java Source construction in "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}
		return sb.toString();
	}

	private void addLine(StringBuilder sb, String value, int tabs) {
		for (int i = 0; i < tabs; i++) {
			sb.append(tab);
		}
		sb.append(value).append(nl);
	}

}
