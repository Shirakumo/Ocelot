package org.shirakumo.lichat;
import org.shirakumo.lichat.conditions.*;
import java.util.*;

public class Reader{
    public static int[] WHITESPACE = new int[]{0x0009, 0x000A, 0x000B, 0x000C, 0x000D, 0x0020, 0x0085, 0x00A0, 0x1680, 0x2000, 0x2001, 0x2002, 0x2003, 0x2004, 0x2005, 0x2006, 0x2008, 0x2009, 0x200A, 0x2028, 0x2029, 0x202F, 0x205F, 0x3000, 0x180E, 0x200B, 0x200C, 0x200D, 0x2060, 0xFEFF};
    public final InputStream stream;
    public static final Symbol invalidSymbol = CL.makeSymbol("INVALID-SYMBOL");

    public Reader(InputStream stream){
        this.stream = stream;
    }

    public Reader(java.io.InputStream stream){
        this.stream = new InputStream(stream);
    }

    public static boolean isWhitespace(int cp){
        for(int ws : WHITESPACE){
            if(ws == cp) return true;
        }
        return false;
    }

    private void skipWhitespace(){
        int prev = stream.read();
        while(isWhitespace(prev)){
            prev = stream.read();
        }
        stream.unread(prev);
    }

    private Symbol safeFindSymbol(String name, String pkg){
        if(pkg == null){
            return CL.makeSymbol(name);
        }else{
            Symbol found = CL.findSymbol(name, pkg);
            if(found == null) found = invalidSymbol;
            return found;
        }
    }

    private List<Object> readSexprList(){
        List<Object> list = new ArrayList<Object>();
        skipWhitespace();
        while(stream.peek() != 41){ /* ) */
            list.add(readSexpr());
            skipWhitespace();
        }
        stream.read();
        if(list.size() == 0){
            return null;
        }else{
            return list;
        }
    }

    private String readSexprString(){
        StringBuffer out = new StringBuffer();
        loop:
        while(true){
            int c = stream.read();
            switch(c){
            case 92:  /* \ */
                out.append(Character.toChars(stream.read()));
                break;
            case 34: /* " */
                break loop;
            default:
                out.append(Character.toChars(c));
            }
        }
        return out.toString();
    }

    private Symbol readSexprKeyword(){
        return safeFindSymbol(readSexprToken(), "KEYWORD");
    }

    private Number readSexprNumber(){
        StringBuffer out = new StringBuffer();
        boolean point = false;
        loop:
        for(int i=0;; i++){
            int c = stream.readNoError();
            switch(c){
            case -1: break loop;
            case 46: /* . */
                if(point){
                    stream.unread(c);
                    break loop;
                }
                point = true;
            case 48: /* 0 */
            case 49: /* 1 */
            case 50: /* 2 */
            case 51: /* 3 */
            case 52: /* 4 */
            case 53: /* 5 */
            case 54: /* 6 */
            case 55: /* 7 */
            case 56: /* 8 */
            case 57: /* 9 */
                out.append(Character.toChars(c));
                break;
            default:
                stream.unread(c);
                break loop;
            }
        }
        String numberString = out.toString();
        if(point){
            return Double.parseDouble(numberString);
        }else{
            return Long.parseLong(numberString);
        }
    }

    private String readSexprToken(){
        stream.peek();
        StringBuffer out = new StringBuffer();
        loop:
        while(true){
            int c = stream.readNoError();
            switch(c){
            case -1: break loop;
            case 92: /* \ */
                out.append(Character.toChars(stream.read()));
                break;
            case 40: /* ( */
            case 41: /* ) */
            case 32: /*   */
            case 34: /* " */
            case 58: /* : */
            case 46: /* . */
            case 48: /* 0 */
            case 49: /* 1 */
            case 50: /* 2 */
            case 51: /* 3 */
            case 52: /* 4 */
            case 53: /* 5 */
            case 54: /* 6 */
            case 55: /* 7 */
            case 56: /* 8 */
            case 57: /* 9 */
                stream.unread(c);
                break loop;
            default:
                out.append(Character.toChars(Character.toUpperCase(c)));
            }
        }
        return out.toString();
    }

    private Symbol readSexprSymbol(){
        String token = readSexprToken();
        if(stream.peekNoError() == 58){ /* : */
            stream.read();
            if(token.equals("#")){
                return safeFindSymbol(readSexprToken(), null);
            }else{
                return safeFindSymbol(readSexprToken(), token);
            }
        }else{
            return safeFindSymbol(token, "LICHAT-PROTOCOL");
        }
    }

    private Object readSexpr(){
        skipWhitespace();
        int c = stream.read();
        switch(c){
        case 40: /* ( */
            return readSexprList();
        case 41: /* ) */
            throw new UnmatchedCloseParen();
        case 34: /* " */
            return readSexprString();
        case 46: /* . */
        case 48: /* 0 */
        case 49: /* 1 */
        case 50: /* 2 */
        case 51: /* 3 */
        case 52: /* 4 */
        case 53: /* 5 */
        case 54: /* 6 */
        case 55: /* 7 */
        case 56: /* 8 */
        case 57: /* 9 */
            stream.unread(c);
            return readSexprNumber();
        case 58: /* : */
            return readSexprKeyword();
        default:
            stream.unread(c);
            return readSexprSymbol();
        }
    }

    public Object fromWire(){
        try{
            Object sexpr = readSexpr();
            if(sexpr instanceof List){
                List<Object> list = (List<Object>)sexpr;
                Object type = list.get(0);
                if(!(type instanceof Symbol))
                    throw new MalformedWireObject(list);

                Map<String, Object> initargs = new HashMap<String, Object>();
                for(int i=1; i<list.size(); i+=2){
                    Object key = list.get(i);
                    Object val = list.get(i+1);
                    if(!(key instanceof Symbol) || !((Symbol)key).pkg.name.equals("KEYWORD"))
                        throw new MalformedWireObject(list);
                    initargs.put(((Symbol)key).name.toLowerCase(), val);
                }

                return CL.makeInstance(CL.findClass((Symbol)type), initargs);
            }else{
                return sexpr;
            }
        }finally{
            while(0 < stream.readNoError());
        }
    }

    public void close(){
        stream.close();
    }
}
