package com.jsoniter.output;

public class CodegenResult {

    String prelude = null; // first
    String epilogue = null; // last
    private StringBuilder lines = new StringBuilder();
    private StringBuilder buffered = new StringBuilder();

    public static String bufferToWriteOp(String buffered) {
        if (buffered == null) {
            return "";
        }
        if (buffered.length() == 1) {
        	
        	if( buffered.charAt(0) == ',')
        	{
        		return "stream.writeByte(com.jsoniter.output.JsonStream.COMMA);";
        	}
        	else if( buffered.charAt(0) == '{')
        	{
        		return "stream.writeByte(com.jsoniter.output.JsonStream.OBJECT_START);";
        	}
        	else if( buffered.charAt(0) == '}')
        	{
        		return "stream.writeByte(com.jsoniter.output.JsonStream.OBJECT_END);";
        	}
        	else if( buffered.charAt(0) == '[')
        	{
        		return "stream.writeByte(com.jsoniter.output.JsonStream.ARRAY_START);";
        	}
        	else if( buffered.charAt(0) == ']')
        	{
        		return "stream.writeByte(com.jsoniter.output.JsonStream.ARRAY_END);";
        	}
        	else if( buffered.charAt(0) == ':')
        	{
        		return "stream.writeByte(com.jsoniter.output.JsonStream.SEMI);";
        	}
        	
            return String.format("stream.writeByte('%s');", escape(buffered.charAt(0)));
        } else if (buffered.length() == 2) {
            return String.format("stream.write((byte)'%s', (byte)'%s');",
                    escape(buffered.charAt(0)), escape(buffered.charAt(1)));
        } else if (buffered.length() == 3) {
            return String.format("stream.write((byte)'%s', (byte)'%s', (byte)'%s');",
                    escape(buffered.charAt(0)), escape(buffered.charAt(1)), escape(buffered.charAt(2)));
        } else if (buffered.length() == 4) {
            return String.format("stream.write((byte)'%s', (byte)'%s', (byte)'%s', (byte)'%s');",
                    escape(buffered.charAt(0)), escape(buffered.charAt(1)), escape(buffered.charAt(2)), escape(buffered.charAt(3)));
        } else {
            StringBuilder escaped = new StringBuilder();
            for (int i = 0; i < buffered.length(); i++) {
                char c = buffered.charAt(i);
                if (c == '"') {
                    escaped.append('\\');
                }
                escaped.append(c);
            }
            return String.format("stream.writeRaw(\"%s\", %s);", escaped.toString(), buffered.length());
        }
    }

    private static String escape(char c) {
        if (c == '"') {
            return "\\\"";
        }
        if (c == '\\') {
            return "\\\\";
        }
        return String.valueOf(c);
    }

    public void append(String str) {
        if (str.contains("stream")) {
            // maintain the order of write op
            // must flush now
            appendBuffer();
        }
        lines.append(str);
        lines.append("\n");
    }

    public void buffer(char c) {
        buffered.append(c);
    }

    public void buffer(String s) {
        if (s == null) {
            return;
        }
        buffered.append(s);
    }

    public void flushBuffer() {
        if (buffered.length() == 0) {
            return;
        }
        if (prelude == null) {
            prelude = buffered.toString();
        } else {
            epilogue = buffered.toString();
        }
        buffered.setLength(0);
    }

    public String toString() {
        return lines.toString();
    }

    public void appendBuffer() {
        flushBuffer();
        if (epilogue != null) {
            lines.append(bufferToWriteOp(epilogue));
            lines.append("\n");
            epilogue = null;
        }
    }

    public String generateWrapperCode(Class clazz) {
        flushBuffer();
        StringBuilder lines = new StringBuilder();
        append(lines, "public void encode(Object obj, com.jsoniter.output.JsonStream stream) throws java.io.IOException {");
        append(lines, "if (obj == null) { stream.writeNull(); return; }");
        if (prelude != null) {
            append(lines, CodegenResult.bufferToWriteOp(prelude));
        }
        append(lines, String.format("encode_((%s)obj, stream);", clazz.getCanonicalName()));
        if (epilogue != null) {
            append(lines, CodegenResult.bufferToWriteOp(epilogue));
        }
        append(lines, "}");
        return lines.toString();
    }
    
 

    private static void append(StringBuilder lines, String line) {
        lines.append(line);
        lines.append('\n');
    }
}
