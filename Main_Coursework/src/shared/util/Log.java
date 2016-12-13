package shared.util;

import java.io.PrintStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Log File
 *
 * @author Huw Jones
 * @since 05/12/2016
 */
public class Log {

    private static Level logLevel = Level.TRACE;

    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static final SimpleDateFormat ms = new SimpleDateFormat("SSS");

    public static void setLogLevel(Level level){
        logLevel = level;
    }

    public static void Fatal(String message){
        message(message, Level.FATAL);
    }

    public static void Error(String message){
        message(message, Level.ERROR);
    }

    public static void Warn(String message){
        message(message, Level.WARN);
    }

    public static void Trace(String message){
        message(message, Level.TRACE);
    }

    public static void Debug(String message){
        message(message, Level.DEBUG);
    }

    public static void Info(String message){
        message(message, Level.INFO);
    }

    public static void Info(String message, boolean printLine){
        message(message, Level.INFO, printLine);
    }

    private static void message(String message, Level level){
        message(message, level, true);
    }
    private static void message(String message, Level level, boolean printLine){
        if(!shouldPrint(level)) return;
        PrintStream out = (level == Level.INFO)? System.out : System.err;
        String output = String.format("[%s][%s][%s]:\t%s", getDateTime(), Thread.currentThread().getName(), level, message);
        if(printLine){
            out.println(output);
        } else {
            out.print(output);
        }
    }

    private static boolean shouldPrint(Level level){
        if(logLevel == Level.NONE) return false;

        if(logLevel == Level.FATAL){
            switch (level){
                case FATAL:
                    return true;
                default:
                    return false;
            }
        }
        if(logLevel == Level.ERROR){
            switch (level){
                case FATAL:
                case ERROR:
                    return true;
                default:
                    return false;
            }
        }
        if(logLevel == Level.WARN){
            switch (level){
                case FATAL:
                case ERROR:
                case WARN:
                    return true;
                default:
                    return false;
            }
        }
        if(logLevel == Level.INFO){
            switch (level){
                case FATAL:
                case ERROR:
                case WARN:
                case INFO:
                    return true;
                default:
                    return false;
            }
        }
        if(logLevel == Level.DEBUG){
            switch (level){
                case FATAL:
                case ERROR:
                case WARN:
                case INFO:
                case DEBUG:
                    return true;
                default:
                    return false;
            }
        }
        if(logLevel == Level.TRACE){
            switch (level){
                case FATAL:
                case ERROR:
                case WARN:
                case INFO:
                case DEBUG:
                case TRACE:
                    return true;
                default:
                    return false;
            }
        }
        return true;
    }

    private static String getDateTime(){
        return format.format(new Date()) + "." + String.format("%03.0f", new Timestamp(new Date().getTime()).getNanos() / 1000000d);
    }

    private enum Level{
        FATAL,
        ERROR,
        WARN,
        INFO,
        TRACE,
        DEBUG,
        NONE
    }
}
