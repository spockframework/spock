// class version 52.0 (52)
// access flags 0x21
public class apackage/TestSpec extends spock/lang/Specification implements groovy/lang/GroovyObject {

  // compiled from: script.groovy

  // access flags 0x1
  public $spock_feature_0_0()V
    TRYCATCHBLOCK L0 L1 L1 java/lang/Throwable
    TRYCATCHBLOCK L0 L1 L2 null
    TRYCATCHBLOCK L1 L3 L2 null
    TRYCATCHBLOCK L4 L5 L5 java/lang/Throwable
    TRYCATCHBLOCK L4 L5 L6 null
    TRYCATCHBLOCK L5 L7 L6 null
    TRYCATCHBLOCK L8 L9 L10 null
   L11
    NOP
    INVOKESTATIC apackage/TestSpec.$getCallSiteArray ()[Lorg/codehaus/groovy/runtime/callsite/CallSite;
    ASTORE 1
   L8
    ALOAD 1
    LDC 0
    AALOAD
    LDC Lorg/spockframework/runtime/ErrorRethrower;.class
    INVOKEINTERFACE org/codehaus/groovy/runtime/callsite/CallSite.callGetProperty (Ljava/lang/Object;)Ljava/lang/Object; (itf)
    LDC Lorg/spockframework/runtime/ErrorCollector;.class
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.castToType (Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;
    CHECKCAST org/spockframework/runtime/ErrorCollector
    ASTORE 2
   L12
    ALOAD 2
    POP
    ALOAD 1
    LDC 1
    AALOAD
    LDC Lorg/spockframework/runtime/ValueRecorder;.class
    INVOKEINTERFACE org/codehaus/groovy/runtime/callsite/CallSite.callConstructor (Ljava/lang/Object;)Ljava/lang/Object; (itf)
    LDC Lorg/spockframework/runtime/ValueRecorder;.class
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.castToType (Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;
    CHECKCAST org/spockframework/runtime/ValueRecorder
    ASTORE 3
   L13
    ALOAD 3
    POP
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    LDC Lorg/spockframework/runtime/SpecificationContext;.class
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.castToType (Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;
    CHECKCAST org/spockframework/runtime/SpecificationContext
    ALOAD 1
    LDC 2
    AALOAD
    LDC Lorg/spockframework/runtime/model/BlockInfo;.class
    ALOAD 1
    LDC 3
    AALOAD
    LDC Lorg/spockframework/runtime/model/BlockKind;.class
    INVOKEINTERFACE org/codehaus/groovy/runtime/callsite/CallSite.callGetProperty (Ljava/lang/Object;)Ljava/lang/Object; (itf)
    ICONST_0
    ANEWARRAY java/lang/Object
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.createList ([Ljava/lang/Object;)Ljava/util/List;
    INVOKEINTERFACE org/codehaus/groovy/runtime/callsite/CallSite.callConstructor (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object; (itf)
    LDC Lorg/spockframework/runtime/model/BlockInfo;.class
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.castToType (Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;
    CHECKCAST org/spockframework/runtime/model/BlockInfo
    INVOKESTATIC org/spockframework/runtime/SpockRuntime.callEnterBlock (Lorg/spockframework/runtime/SpecificationContext;Lorg/spockframework/runtime/model/BlockInfo;)V
    ACONST_NULL
    POP
   L0
    LINENUMBER 4 L0
    ALOAD 2
    ALOAD 3
    INVOKEVIRTUAL org/spockframework/runtime/ValueRecorder.reset ()Lorg/spockframework/runtime/ValueRecorder;
    LDC "true"
    ICONST_4
    ICONST_5
    ACONST_NULL
    ALOAD 3
    ALOAD 3
    ICONST_0
    INVOKEVIRTUAL org/spockframework/runtime/ValueRecorder.startRecordingValue (I)I
    ICONST_1
    INVOKESTATIC java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
    INVOKEVIRTUAL org/spockframework/runtime/ValueRecorder.record (ILjava/lang/Object;)Ljava/lang/Object;
    INVOKESTATIC org/spockframework/runtime/SpockRuntime.verifyCondition (Lorg/spockframework/runtime/ErrorCollector;Lorg/spockframework/runtime/ValueRecorder;Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V
    ACONST_NULL
    POP
    GOTO L14
   L1
   FRAME FULL [apackage/TestSpec [Lorg/codehaus/groovy/runtime/callsite/CallSite; org/spockframework/runtime/ErrorCollector org/spockframework/runtime/ValueRecorder] [java/lang/Throwable]
    ASTORE 4
   L15
    ALOAD 2
    ALOAD 3
    LDC "true"
    ICONST_4
    ICONST_5
    ACONST_NULL
    ALOAD 4
    INVOKESTATIC org/spockframework/runtime/SpockRuntime.conditionFailedWithException (Lorg/spockframework/runtime/ErrorCollector;Lorg/spockframework/runtime/ValueRecorder;Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Throwable;)V
    ACONST_NULL
    POP
    NOP
   L3
    GOTO L14
   L14
   FRAME SAME
    GOTO L16
   L2
   FRAME SAME1 java/lang/Throwable
    ASTORE 5
    ALOAD 5
    ATHROW
   L16
   FRAME SAME
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    LDC Lorg/spockframework/runtime/SpecificationContext;.class
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.castToType (Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;
    CHECKCAST org/spockframework/runtime/SpecificationContext
    ACONST_NULL
    LDC Ljava/lang/Throwable;.class
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.castToType (Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;
    CHECKCAST java/lang/Throwable
    INVOKEVIRTUAL org/spockframework/runtime/SpecificationContext.setThrownException (Ljava/lang/Throwable;)V
    ACONST_NULL
    POP
   L4
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    LDC Lorg/spockframework/runtime/SpecificationContext;.class
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.castToType (Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;
    CHECKCAST org/spockframework/runtime/SpecificationContext
    ALOAD 1
    LDC 4
    AALOAD
    LDC Lorg/spockframework/runtime/model/BlockInfo;.class
    ALOAD 1
    LDC 5
    AALOAD
    LDC Lorg/spockframework/runtime/model/BlockKind;.class
    INVOKEINTERFACE org/codehaus/groovy/runtime/callsite/CallSite.callGetProperty (Ljava/lang/Object;)Ljava/lang/Object; (itf)
    ICONST_0
    ANEWARRAY java/lang/Object
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.createList ([Ljava/lang/Object;)Ljava/util/List;
    INVOKEINTERFACE org/codehaus/groovy/runtime/callsite/CallSite.callConstructor (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object; (itf)
    LDC Lorg/spockframework/runtime/model/BlockInfo;.class
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.castToType (Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;
    CHECKCAST org/spockframework/runtime/model/BlockInfo
    INVOKESTATIC org/spockframework/runtime/SpockRuntime.callEnterBlock (Lorg/spockframework/runtime/SpecificationContext;Lorg/spockframework/runtime/model/BlockInfo;)V
    ACONST_NULL
    POP
   L17
    LINENUMBER 6 L17
    ICONST_1
    POP
    GOTO L18
   L5
   FRAME SAME1 java/lang/Throwable
    ASTORE 6
   L19
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    LDC Lorg/spockframework/runtime/SpecificationContext;.class
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.castToType (Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;
    CHECKCAST org/spockframework/runtime/SpecificationContext
    ALOAD 6
    INVOKEVIRTUAL org/spockframework/runtime/SpecificationContext.setThrownException (Ljava/lang/Throwable;)V
    ACONST_NULL
    POP
    NOP
   L7
    GOTO L18
   L18
   FRAME SAME
    GOTO L20
   L6
   FRAME SAME1 java/lang/Throwable
    ASTORE 7
    ALOAD 7
    ATHROW
   L20
   FRAME SAME
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    LDC Lorg/spockframework/runtime/SpecificationContext;.class
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.castToType (Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;
    CHECKCAST org/spockframework/runtime/SpecificationContext
    ALOAD 1
    LDC 6
    AALOAD
    LDC Lorg/spockframework/runtime/model/BlockInfo;.class
    ALOAD 1
    LDC 7
    AALOAD
    LDC Lorg/spockframework/runtime/model/BlockKind;.class
    INVOKEINTERFACE org/codehaus/groovy/runtime/callsite/CallSite.callGetProperty (Ljava/lang/Object;)Ljava/lang/Object; (itf)
    ICONST_0
    ANEWARRAY java/lang/Object
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.createList ([Ljava/lang/Object;)Ljava/util/List;
    INVOKEINTERFACE org/codehaus/groovy/runtime/callsite/CallSite.callConstructor (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object; (itf)
    LDC Lorg/spockframework/runtime/model/BlockInfo;.class
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.castToType (Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;
    CHECKCAST org/spockframework/runtime/model/BlockInfo
    INVOKESTATIC org/spockframework/runtime/SpockRuntime.callEnterBlock (Lorg/spockframework/runtime/SpecificationContext;Lorg/spockframework/runtime/model/BlockInfo;)V
    ACONST_NULL
    POP
   L21
    LINENUMBER 8 L21
    ALOAD 1
    LDC 8
    AALOAD
    ALOAD 0
    ACONST_NULL
    ACONST_NULL
    LDC Ljava/lang/RuntimeException;.class
    INVOKEINTERFACE org/codehaus/groovy/runtime/callsite/CallSite.callCurrent (Lgroovy/lang/GroovyObject;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object; (itf)
    POP
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    LDC Lorg/spockframework/runtime/SpecificationContext;.class
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.castToType (Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;
    CHECKCAST org/spockframework/runtime/SpecificationContext
    INVOKEVIRTUAL org/spockframework/runtime/SpecificationContext.getMockController ()Lorg/spockframework/mock/IMockController;
    LDC Lorg/spockframework/mock/runtime/MockController;.class
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.castToType (Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;
    CHECKCAST org/spockframework/mock/runtime/MockController
    INVOKEVIRTUAL org/spockframework/mock/runtime/MockController.leaveScope ()V
    ACONST_NULL
    POP
   L22
    GOTO L9
   L9
   FRAME SAME
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    LDC Lorg/spockframework/runtime/SpecificationContext;.class
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.castToType (Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;
    CHECKCAST org/spockframework/runtime/SpecificationContext
    INVOKESTATIC org/spockframework/runtime/SpockRuntime.clearCurrentBlock (Lorg/spockframework/runtime/SpecificationContext;)V
    ACONST_NULL
    POP
    GOTO L23
   L10
   FRAME FULL [apackage/TestSpec [Lorg/codehaus/groovy/runtime/callsite/CallSite;] [java/lang/Throwable]
    ASTORE 8
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    LDC Lorg/spockframework/runtime/SpecificationContext;.class
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.castToType (Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;
    CHECKCAST org/spockframework/runtime/SpecificationContext
    INVOKESTATIC org/spockframework/runtime/SpockRuntime.clearCurrentBlock (Lorg/spockframework/runtime/SpecificationContext;)V
    ACONST_NULL
    POP
    ALOAD 8
    ATHROW
   L23
   FRAME APPEND [org/spockframework/runtime/ErrorCollector org/spockframework/runtime/ValueRecorder]
    RETURN
    LOCALVARIABLE this Lapackage/TestSpec; L11 L23 0
    LOCALVARIABLE $spock_errorCollector Lorg/spockframework/runtime/ErrorCollector; L12 L22 2
    LOCALVARIABLE $spock_valueRecorder Lorg/spockframework/runtime/ValueRecorder; L13 L22 3
    LOCALVARIABLE $spock_condition_throwable Ljava/lang/Throwable; L15 L3 4
    LOCALVARIABLE $spock_ex Ljava/lang/Throwable; L19 L7 6
    MAXSTACK = 9
    MAXLOCALS = 9
}