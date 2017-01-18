package com.jsoniter;

import java.io.IOException;

public class IterImplObject {

    public static final String readObject(JsonIterator iter) throws IOException {
        byte c = IterImpl.nextToken(iter);
        switch (c) {
            case 'n':
                IterImpl.skipFixedBytes(iter, 3);
                return null;
            case '{':
                c = IterImpl.nextToken(iter);
                if (c == '"') {
                    iter.unreadByte();
                    String field = iter.readString();
                    if (IterImpl.nextToken(iter) != ':') {
                        throw iter.reportError("readObject", "expect :");
                    }
                    return field;
                }
                if (c == '}') {
                    return null; // end of object
                }
                throw iter.reportError("readObject", "expect \" after {");
            case ',':
                String field = iter.readString();
                if (IterImpl.nextToken(iter) != ':') {
                    throw iter.reportError("readObject", "expect :");
                }
                return field;
            case '}':
                return null; // end of object
            default:
                throw iter.reportError("readObject", "expect { or , or } or n");
        }
    }

    public static final void readObjectCB(JsonIterator iter, JsonIterator.ReadObjectCallback cb) throws IOException {
        byte c = IterImpl.nextToken(iter);
        if ('{' == c) {
            c = IterImpl.nextToken(iter);
            if ('"' == c) {
                iter.unreadByte();
                String field = iter.readString();
                if (IterImpl.nextToken(iter) != ':') {
                    throw iter.reportError("readObject", "expect :");
                }
                if (!cb.handle(iter, field)) {
                    return;
                }
                while (IterImpl.nextToken(iter) == ',') {
                    field = iter.readString();
                    if (IterImpl.nextToken(iter) != ':') {
                        throw iter.reportError("readObject", "expect :");
                    }
                    if (!cb.handle(iter, field)) {
                        return;
                    }
                }
                return;
            }
            if ('}' == c) {
                return;
            }
            throw iter.reportError("readObjectCB", "expect \" after {");
        }
        if ('n' == c) {
            IterImpl.skipFixedBytes(iter, 3);
            return;
        }
        throw iter.reportError("readObjectCB", "expect { or n");
    }
}