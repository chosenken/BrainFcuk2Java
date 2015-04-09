# BrainFcuk2Java
A Brain Fuck compiler that compiles down to Java Source and Java Byte Code.

But...Why?
--------

Because......  I've always liked BrainFuck, it is the most weird, useless, awesome language I have found.  Plus it is really simple to implement with their only being 8 opt codes.

Usage
--------

    BrainFcuk2Java [source]
     -D   prints debug information
     -S   output java source code
     -v   be verbose
     

Known Issues
--------

Right now "," is not working as the created Java code isn't reading in characters correctly.  I am working on it, but for now just don't use a BrainFuck app that needs to read in from stdin.

How does it work?
--------

The application is actually quite simple.  It reads in the passed in BrainFuck source file and processes the file opcode to opcode.  It ignores none BrainFuck characters (+-<>[],.), creating a stack of opcodes which then get translated into Java code.  It does perform some minor optimizations, primarily condensing additions/subtractions and pointer moves.  

If you include the -S command, along with the compiled class you will also receive the Java source code, pre-formatted.
