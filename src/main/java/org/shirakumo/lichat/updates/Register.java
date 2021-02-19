package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Register extends Update{
    public static final Symbol className;
    static{
        className = CL.intern("REGISTER");
        CL.registerClass(className, Register.class);
    }

    public final String password;

    public Register(Map<String, Object> initargs){
        super(initargs);
        password = (String)CL.requiredArg(initargs, "password");
    }
}
