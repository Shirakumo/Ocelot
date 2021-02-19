package org.shirakumo.lichat;
import org.shirakumo.lichat.conditions.*;
import java.util.*;

public class Printer{
    public final OutputStream stream;

    public Printer(OutputStream stream){
        this.stream = stream;
    }

    public Printer(java.io.OutputStream stream){
        this.stream = new OutputStream(stream);
    }

    private void printSexprList(List<Object> list){
        stream.write("(");
        try{
            for(int i=0; i<list.size(); i++){
                printSexpr(list.get(i));
                if(i+1 < list.size())
                    stream.write(" ");
            }
        }finally{
            stream.write(")");
        }
    }

    private void printSexprString(String string){
        stream.write("\"");
        try{
            final int length = string.length();
            for(int offset = 0; offset < length;){
                final int cp = string.codePointAt(offset);
                
                if(cp == 92 /* \ */ || cp == 34 /* " */){
                    stream.write("\\");
                }
                if(cp != 0){
                    stream.write(cp);
                }
                
                offset += Character.charCount(cp);
            }
        }finally{
            stream.write("\"");
        }
    }

    private void printSexprNumber(Number number){
        if(number instanceof Double)
            stream.write(new java.math.BigDecimal((Double)number).stripTrailingZeros().toPlainString());
        else if(number instanceof Float)
            stream.write(new java.math.BigDecimal((Float)number).stripTrailingZeros().toPlainString());
        else if(number instanceof Integer)
            stream.write(((Integer)number).toString());
        else if(number instanceof Long)
            stream.write(((Long)number).toString());
        else if(number instanceof Short)
            stream.write(((Short)number).toString());
    }

    private void printSexprToken(String token){
        final int length = token.length();
        for(int offset = 0; offset < length;){
            final int cp = token.codePointAt(offset);
            
            switch(cp){
            case 0: break;
            case 92: /* \ */
            case 35: /* # */
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
                stream.write("\\");
                break;
            }
            stream.write(cp);
            
            offset += Character.charCount(cp);
        }
    }

    private void printSexprSymbol(Symbol symbol){
        if(symbol.pkg == null){
            stream.write("#:");
        }else if(symbol.pkg == CL.findPackage("KEYWORD")){
            stream.write(":");
        }else if(symbol.pkg == CL.findPackage("LICHAT-PROTOCOL")){
        }else{
            printSexprToken(symbol.pkg.name);
            stream.write(":");
        }
        printSexprToken(symbol.name);
    }

    private void printSexpr(Object sexpr){
        if(sexpr == null){
            printSexprToken("NIL");
        }else if(sexpr instanceof String){
            printSexprString((String)sexpr);
        }else if(sexpr instanceof List){
            printSexprList((List<Object>)sexpr);
        }else if(sexpr instanceof Number){
            printSexprNumber((Number)sexpr);
        }else if(sexpr instanceof Symbol){
            printSexprSymbol((Symbol)sexpr);
        }else{
            throw new UnprintableObject(sexpr);
        }
    }

    private String slotToSymbolName(String slot){
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<slot.length(); i++){
            char c = slot.charAt(i);
            if(Character.isUpperCase(c)) builder.append('-');
            builder.append(Character.toLowerCase(c));
        }
        return builder.toString();
    }

    public void toWire(Object object){
        if(object instanceof StandardObject){
            StandardObject wireable = (StandardObject)object;
            List<Object> list = new ArrayList<Object>();
            list.add(CL.className(CL.classOf(wireable)));
            for(String slot : CL.classSlots(wireable.getClass())){
                list.add(CL.findSymbol(slotToSymbolName(slot), "KEYWORD"));
                list.add(CL.slotValue(wireable, slot));
            }
            printSexpr(list);
        }else{
            printSexpr(object);
        }
        stream.write(0);
        stream.flush();
    }

    public void close(){
        stream.close();
    }
}
