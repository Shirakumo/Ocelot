package org.shirakumo.lichat;
import org.shirakumo.lichat.updates.*;
import org.shirakumo.lichat.conditions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class CL{
    private static final Map<String, Package> packages;
    private static final Map<Symbol, Class<? extends StandardObject>> classes;
    private static final Map<Class<? extends StandardObject>, Symbol> classNames;
    public static Package PACKAGE;

    static{
        packages = new HashMap<String, Package>();
        classes = new HashMap<Symbol, Class<? extends StandardObject>>();
        classNames = new HashMap<Class<? extends StandardObject>, Symbol>();
        PACKAGE = makePackage("LICHAT-PROTOCOL");
        makePackage("KEYWORD");

        for(String name : new String[]{"ID","CLOCK","FROM","PASSWORD","VERSION","EXTENSIONS","CHANNEL","TARGET","TEXT","PERMISSIONS","USERS","CHANNELS","REGISTERED","CONNECTIONS","UPDATE-ID","COMPATIBLE-VERSIONS","CONTENT-TYPE","FILENAME","PAYLOAD","ALLOWED-CONTENT-TYPES", "BY", "KEY", "RULE", "KEYS", "UPDATE", "UPDATES", "ATTRIBUTES", "IP", "MASK"}){
            intern(name, "KEYWORD");
        }

        for(String name : new String[]{"NIL","T", "AND", "OR", "NOT", "+", "-"}){
            intern(name, "LICHAT-PROTOCOL");
        }

        // Java doesn't execute the static blocks in the classes, so they
        // can't register themselves. THANKS A LOT, IDIOTS
        registerClass(intern("ALREADY-IN-CHANNEL"), AlreadyInChannel.class);
        registerClass(intern("BACKFILL"), Backfill.class);
        registerClass(intern("BAD-CONTENT-TYPE"), BadContentType.class);
        registerClass(intern("BAD-NAME"), BadName.class);
        registerClass(intern("BAN"), Ban.class);
        registerClass(intern("CAPABILITIES"), Capabilities.class);
        registerClass(intern("CHANNEL-UPDATE"), ChannelUpdate.class);
        registerClass(intern("CHANNEL-INFO"), ChannelInfo.class);
        registerClass(intern("CHANNELNAME-TAKEN"), ChannelnameTaken.class);
        registerClass(intern("CHANNELS"), Channels.class);
        registerClass(intern("CONNECT"), Connect.class);
        registerClass(intern("CONNECTION-UNSTABLE"), ConnectionUnstable.class);
        registerClass(intern("CONNECTION-LOST"), ConnectionLost.class);
        registerClass(intern("CREATE"), Create.class);
        registerClass(intern("DATA"), Data.class);
        registerClass(intern("DENY"), Deny.class);
        registerClass(intern("DESTROY"), Destroy.class);
        registerClass(intern("DISCONNECT"), Disconnect.class);
        registerClass(intern("EDIT"), Edit.class);
        registerClass(intern("EMOTE"), Emote.class);
        registerClass(intern("EMOTES"), Emotes.class);
        registerClass(intern("FAILURE"), Failure.class);
        registerClass(intern("GRANT"), Grant.class);
        registerClass(intern("INCOMPATIBLE-VERSION"), IncompatibleVersion.class);
        registerClass(intern("INSUFFICIENT-PERMISSIONS"), InsufficientPermissions.class);
        registerClass(intern("INVALID-PASSWORD"), InvalidPassword.class);
        registerClass(intern("INVALID-PERMISSIONS"), InvalidPermissions.class);
        registerClass(intern("INVALID-UPDATE"), InvalidUpdate.class);
        registerClass(intern("IP-BAN"), IpBan.class);
        registerClass(intern("IP-UNBAN"), IpUnban.class);
        registerClass(intern("JOIN"), Join.class);
        registerClass(intern("KICK"), Kick.class);
        registerClass(intern("KILL"), Kill.class);
        registerClass(intern("LEAVE"), Leave.class);
        registerClass(intern("MALFORMED-UPDATE"), MalformedUpdate.class);
        registerClass(intern("MESSAGE"), Message.class);
        registerClass(intern("NO-SUCH-CHANNEL"), NoSuchChannel.class);
        registerClass(intern("NO-SUCH-PROFILE"), NoSuchProfile.class);
        registerClass(intern("NO-SUCH-USER"), NoSuchUser.class);
        registerClass(intern("NOT-IN-CHANNEL"), NotInChannel.class);
        registerClass(intern("PAUSE"), Pause.class);
        registerClass(intern("PERMISSIONS"), Permissions.class);
        registerClass(intern("PING"), Ping.class);
        registerClass(intern("PONG"), Pong.class);
        registerClass(intern("PULL"), Pull.class);
        registerClass(intern("QUIET"), Quiet.class);
        registerClass(intern("REGISTER"), Register.class);
        registerClass(intern("SERVER-INFO"), ServerInfo.class);
        registerClass(intern("SET-CHANNEL-INFO"), SetChannelInfo.class);
        registerClass(intern("TARGET-UPDATE"), TargetUpdate.class);
        registerClass(intern("TEXT-UPDATE"), TextUpdate.class);
        registerClass(intern("TOO-MANY-CONNECTIONS"), TooManyConnections.class);
        registerClass(intern("TOO-MANY-UPDATES"), TooManyUpdates.class);
        registerClass(intern("UNBAN"), Unban.class);
        registerClass(intern("UNQUIET"), Unquiet.class);
        registerClass(intern("UPDATE"), Update.class);
        registerClass(intern("UPDATE-FAILURE"), UpdateFailure.class);
        registerClass(intern("UPDATE-TOO-LONG"), UpdateTooLong.class);
        registerClass(intern("USER-INFO"), UserInfo.class);
        registerClass(intern("USERNAME-MISMATCH"), UsernameMismatch.class);
        registerClass(intern("USERNAME-TAKEN"), UsernameTaken.class);
        registerClass(intern("USERS"), Users.class);
    }

    public static Symbol makeSymbol(String name){
        return new Symbol(null, name);
    }

    public static Symbol intern(String name){
        return intern(name, PACKAGE);
    }

    public static Symbol intern(String name, Package pkg){
        return pkg.intern(name);
    }

    public static Symbol intern(String name, String pkg){
        return packages.get(pkg).intern(name);
    }

    public static Symbol findSymbol(String name){
        return findSymbol(name, PACKAGE);
    }

    public static Symbol findSymbol(String name, Package pkg){
        return pkg.findSymbol(name);
    }

    public static Symbol findSymbol(String name, String pkg){
        return packages.get(pkg).intern(name);
    }

    public static Package findPackage(String name){
        return packages.get(name);
    }

    public static Package makePackage(String name){
        if(packages.get(name) != null)
            throw new PackageAlreadyExists(name);
        Package pkg = new Package(name);
        packages.put(name, pkg);
        return pkg;
    }

    public static StandardObject makeInstance(Symbol name, Object... initargs){
        Map<String, Object> argmap = new HashMap<String, Object>();
        for(int i=0; i<initargs.length; i+=2){
            argmap.put((String)initargs[i], initargs[i+1]);
        }
        return makeInstance(findClass(name), argmap);
    }

    public static StandardObject makeInstance(Class<? extends StandardObject> clas, Map<String, Object> initargs){
        try{
            return clas.getConstructor(Map.class).newInstance(initargs);
        }catch(NoSuchMethodException ex){
            throw new InvalidClassDefinition(clas);
        }catch(InstantiationException ex){
            throw new InstantiationFailed(clas, initargs);
        }catch(IllegalAccessException ex){
            throw new InstantiationFailed(clas, initargs);
        }catch(java.lang.reflect.InvocationTargetException ex){
            if(ex.getCause() instanceof RuntimeException){
                throw (RuntimeException)ex.getCause();
            }else{
                throw new InstantiationFailed(clas, initargs);
            }
        }
    }

    public static Class<? extends StandardObject> registerClass(Symbol name, Class<? extends StandardObject> clas){
        classes.put(name, clas);
        classNames.put(clas, name);
        return clas;
    }

    public static Class<? extends StandardObject> findClass(Symbol name){
        Class<? extends StandardObject> clas = classes.get(name);
        if(clas == null) throw new NoSuchClass(name);
        return clas;
    }

    public static Class<? extends StandardObject> classOf(StandardObject object){
        return object.getClass();
    }

    public static Symbol className(Class<? extends StandardObject> clas){
        return classNames.get(clas);
    }

    public static List<String> classSlots(Class<? extends StandardObject> clas){
        List<String> slots = new ArrayList<String>();
        for(java.lang.reflect.Field field : clas.getFields()){
            if(!java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                slots.add(field.getName());
        }
        return slots;
    }

    public static Object slotValue(StandardObject object, String slot){
        try{
            return object.getClass().getField(slot).get(object);
        }catch(NoSuchFieldException ex){
            throw new SlotMissing(object, slot);
        }catch(IllegalAccessException ex){
            throw new SlotValueFailed(object, slot);
        }
    }

    public static Object requiredArg(Map<String, Object> map, String arg){
        if(!map.containsKey(arg))
            throw new MissingArgument(arg);
        return map.get(arg);
    }

    public static Object arg(Map<String, Object> map, String arg){
        return arg(map, arg, null);
    }

    public static Object arg(Map<String, Object> map, String arg, Object def){
        if(!map.containsKey(arg))
            return def;
        else{
            Object value = map.get(arg);
            if(value == findSymbol("NIL"))
                return null;
            else
                return value;
        }
    }

    public static Condition error(String message){
        throw new Condition(message);
    }

    private static final long universalUnixOffset = 2208988800L;

    public static long getUniversalTime(){
        return (new Date().getTime())/1000 + universalUnixOffset;
    }

    public static long universalToUnix(long universal){
        return universal - universalUnixOffset;
    }

    public static void sleep(float s){
        try{
            Thread.sleep((long)(s*1000));
        }catch(Exception ex){}
    }

    public static void sleep(double s){
        try{
            Thread.sleep((long)(s*1000));
        }catch(Exception ex){}
    }

    public static byte[] readOctetStream(java.io.InputStream in) throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int nRead;
        byte[] buffer = new byte[4096];
        while ((nRead = in.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, nRead);
        }
        in.close();
        return out.toByteArray();
    }
}
