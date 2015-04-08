package org.ken.brainfcuk.opcodes;

public class OpCode {
	public OpCodeEnum type;
	public int value;
	
	public OpCode(OpCodeEnum type, int value) {
		this.type = type;
		this.value = value;
	}
	
	public OpCode(OpCodeEnum type) {
		this.type = type;
	}
}
