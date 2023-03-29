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
   L8
    LDC Lorg/spockframework/runtime/ErrorRethrower;.class
    INVOKEDYNAMIC getProperty(Ljava/lang/Class;)Ljava/lang/Object; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "INSTANCE", 
      0
    ]
    INVOKEDYNAMIC cast(Ljava/lang/Object;)Lorg/spockframework/runtime/ErrorCollector; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    ASTORE 1
   L11
    ALOAD 1
    POP
    LDC Lorg/spockframework/runtime/ValueRecorder;.class
    INVOKEDYNAMIC init(Ljava/lang/Class;)Ljava/lang/Object; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "<init>", 
      0
    ]
    INVOKEDYNAMIC cast(Ljava/lang/Object;)Lorg/spockframework/runtime/ValueRecorder; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    ASTORE 2
   L12
    ALOAD 2
    POP
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    INVOKEDYNAMIC cast(Lorg/spockframework/lang/ISpecificationContext;)Lorg/spockframework/runtime/SpecificationContext; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    LDC Lorg/spockframework/runtime/model/BlockInfo;.class
    LDC Lorg/spockframework/runtime/model/BlockKind;.class
    INVOKEDYNAMIC getProperty(Ljava/lang/Class;)Ljava/lang/Object; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "EXPECT", 
      0
    ]
    ICONST_0
    ANEWARRAY java/lang/Object
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.createList ([Ljava/lang/Object;)Ljava/util/List;
    INVOKEDYNAMIC init(Ljava/lang/Class;Ljava/lang/Object;Ljava/util/List;)Ljava/lang/Object; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "<init>", 
      0
    ]
    INVOKEDYNAMIC cast(Ljava/lang/Object;)Lorg/spockframework/runtime/model/BlockInfo; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    INVOKESTATIC org/spockframework/runtime/SpockRuntime.callEnterBlock (Lorg/spockframework/runtime/SpecificationContext;Lorg/spockframework/runtime/model/BlockInfo;)V
    ACONST_NULL
    POP
   L0
    LINENUMBER 4 L0
    ALOAD 1
    ALOAD 2
    INVOKEVIRTUAL org/spockframework/runtime/ValueRecorder.reset ()Lorg/spockframework/runtime/ValueRecorder;
    LDC "true"
    ICONST_4
    ICONST_5
    ACONST_NULL
    ALOAD 2
    ALOAD 2
    ICONST_0
    INVOKEVIRTUAL org/spockframework/runtime/ValueRecorder.startRecordingValue (I)I
    ICONST_1
    INVOKESTATIC java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
    INVOKEVIRTUAL org/spockframework/runtime/ValueRecorder.record (ILjava/lang/Object;)Ljava/lang/Object;
    INVOKESTATIC org/spockframework/runtime/SpockRuntime.verifyCondition (Lorg/spockframework/runtime/ErrorCollector;Lorg/spockframework/runtime/ValueRecorder;Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V
    ACONST_NULL
    POP
    GOTO L13
   L1
   FRAME FULL [apackage/TestSpec org/spockframework/runtime/ErrorCollector org/spockframework/runtime/ValueRecorder] [java/lang/Throwable]
    ASTORE 3
   L14
    ALOAD 1
    ALOAD 2
    LDC "true"
    ICONST_4
    ICONST_5
    ACONST_NULL
    ALOAD 3
    INVOKESTATIC org/spockframework/runtime/SpockRuntime.conditionFailedWithException (Lorg/spockframework/runtime/ErrorCollector;Lorg/spockframework/runtime/ValueRecorder;Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Throwable;)V
    ACONST_NULL
    POP
    NOP
   L3
    GOTO L13
   L13
   FRAME SAME
    GOTO L15
   L2
   FRAME SAME1 java/lang/Throwable
    ASTORE 4
    ALOAD 4
    ATHROW
   L15
   FRAME SAME
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    INVOKEDYNAMIC cast(Lorg/spockframework/lang/ISpecificationContext;)Lorg/spockframework/runtime/SpecificationContext; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    LDC Lorg/spockframework/runtime/model/BlockInfo;.class
    LDC Lorg/spockframework/runtime/model/BlockKind;.class
    INVOKEDYNAMIC getProperty(Ljava/lang/Class;)Ljava/lang/Object; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "EXPECT", 
      0
    ]
    ICONST_0
    ANEWARRAY java/lang/Object
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.createList ([Ljava/lang/Object;)Ljava/util/List;
    INVOKEDYNAMIC init(Ljava/lang/Class;Ljava/lang/Object;Ljava/util/List;)Ljava/lang/Object; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "<init>", 
      0
    ]
    INVOKEDYNAMIC cast(Ljava/lang/Object;)Lorg/spockframework/runtime/model/BlockInfo; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    INVOKESTATIC org/spockframework/runtime/SpockRuntime.callExitBlock (Lorg/spockframework/runtime/SpecificationContext;Lorg/spockframework/runtime/model/BlockInfo;)V
    ACONST_NULL
    POP
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    INVOKEDYNAMIC cast(Lorg/spockframework/lang/ISpecificationContext;)Lorg/spockframework/runtime/SpecificationContext; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    LDC Lorg/spockframework/runtime/model/BlockInfo;.class
    LDC Lorg/spockframework/runtime/model/BlockKind;.class
    INVOKEDYNAMIC getProperty(Ljava/lang/Class;)Ljava/lang/Object; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "WHEN", 
      0
    ]
    ICONST_0
    ANEWARRAY java/lang/Object
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.createList ([Ljava/lang/Object;)Ljava/util/List;
    INVOKEDYNAMIC init(Ljava/lang/Class;Ljava/lang/Object;Ljava/util/List;)Ljava/lang/Object; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "<init>", 
      0
    ]
    INVOKEDYNAMIC cast(Ljava/lang/Object;)Lorg/spockframework/runtime/model/BlockInfo; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    INVOKESTATIC org/spockframework/runtime/SpockRuntime.callEnterBlock (Lorg/spockframework/runtime/SpecificationContext;Lorg/spockframework/runtime/model/BlockInfo;)V
    ACONST_NULL
    POP
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    INVOKEDYNAMIC cast(Lorg/spockframework/lang/ISpecificationContext;)Lorg/spockframework/runtime/SpecificationContext; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    ACONST_NULL
    INVOKEVIRTUAL org/spockframework/runtime/SpecificationContext.setThrownException (Ljava/lang/Throwable;)V
    ACONST_NULL
    POP
   L4
    LINENUMBER 6 L4
    ICONST_1
    POP
    GOTO L16
   L5
   FRAME SAME1 java/lang/Throwable
    ASTORE 5
   L17
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    INVOKEDYNAMIC cast(Lorg/spockframework/lang/ISpecificationContext;)Lorg/spockframework/runtime/SpecificationContext; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    ALOAD 5
    INVOKEVIRTUAL org/spockframework/runtime/SpecificationContext.setThrownException (Ljava/lang/Throwable;)V
    ACONST_NULL
    POP
    NOP
   L7
    GOTO L16
   L16
   FRAME SAME
    GOTO L18
   L6
   FRAME SAME1 java/lang/Throwable
    ASTORE 6
    ALOAD 6
    ATHROW
   L18
   FRAME SAME
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    INVOKEDYNAMIC cast(Lorg/spockframework/lang/ISpecificationContext;)Lorg/spockframework/runtime/SpecificationContext; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    LDC Lorg/spockframework/runtime/model/BlockInfo;.class
    LDC Lorg/spockframework/runtime/model/BlockKind;.class
    INVOKEDYNAMIC getProperty(Ljava/lang/Class;)Ljava/lang/Object; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "WHEN", 
      0
    ]
    ICONST_0
    ANEWARRAY java/lang/Object
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.createList ([Ljava/lang/Object;)Ljava/util/List;
    INVOKEDYNAMIC init(Ljava/lang/Class;Ljava/lang/Object;Ljava/util/List;)Ljava/lang/Object; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "<init>", 
      0
    ]
    INVOKEDYNAMIC cast(Ljava/lang/Object;)Lorg/spockframework/runtime/model/BlockInfo; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    INVOKESTATIC org/spockframework/runtime/SpockRuntime.callExitBlock (Lorg/spockframework/runtime/SpecificationContext;Lorg/spockframework/runtime/model/BlockInfo;)V
    ACONST_NULL
    POP
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    INVOKEDYNAMIC cast(Lorg/spockframework/lang/ISpecificationContext;)Lorg/spockframework/runtime/SpecificationContext; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    LDC Lorg/spockframework/runtime/model/BlockInfo;.class
    LDC Lorg/spockframework/runtime/model/BlockKind;.class
    INVOKEDYNAMIC getProperty(Ljava/lang/Class;)Ljava/lang/Object; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "THEN", 
      0
    ]
    ICONST_0
    ANEWARRAY java/lang/Object
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.createList ([Ljava/lang/Object;)Ljava/util/List;
    INVOKEDYNAMIC init(Ljava/lang/Class;Ljava/lang/Object;Ljava/util/List;)Ljava/lang/Object; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "<init>", 
      0
    ]
    INVOKEDYNAMIC cast(Ljava/lang/Object;)Lorg/spockframework/runtime/model/BlockInfo; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    INVOKESTATIC org/spockframework/runtime/SpockRuntime.callEnterBlock (Lorg/spockframework/runtime/SpecificationContext;Lorg/spockframework/runtime/model/BlockInfo;)V
    ACONST_NULL
    POP
   L19
    LINENUMBER 8 L19
    ALOAD 0
    ACONST_NULL
    ACONST_NULL
    LDC Ljava/lang/RuntimeException;.class
    INVOKEDYNAMIC invoke(Lapackage/TestSpec;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "thrownImpl", 
      2
    ]
    POP
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    INVOKEDYNAMIC cast(Lorg/spockframework/lang/ISpecificationContext;)Lorg/spockframework/runtime/SpecificationContext; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    LDC Lorg/spockframework/runtime/model/BlockInfo;.class
    LDC Lorg/spockframework/runtime/model/BlockKind;.class
    INVOKEDYNAMIC getProperty(Ljava/lang/Class;)Ljava/lang/Object; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "THEN", 
      0
    ]
    ICONST_0
    ANEWARRAY java/lang/Object
    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.createList ([Ljava/lang/Object;)Ljava/util/List;
    INVOKEDYNAMIC init(Ljava/lang/Class;Ljava/lang/Object;Ljava/util/List;)Ljava/lang/Object; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "<init>", 
      0
    ]
    INVOKEDYNAMIC cast(Ljava/lang/Object;)Lorg/spockframework/runtime/model/BlockInfo; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    INVOKESTATIC org/spockframework/runtime/SpockRuntime.callExitBlock (Lorg/spockframework/runtime/SpecificationContext;Lorg/spockframework/runtime/model/BlockInfo;)V
    ACONST_NULL
    POP
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    INVOKEDYNAMIC cast(Lorg/spockframework/lang/ISpecificationContext;)Lorg/spockframework/runtime/SpecificationContext; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    INVOKEVIRTUAL org/spockframework/runtime/SpecificationContext.getMockController ()Lorg/spockframework/mock/IMockController;
    INVOKEDYNAMIC cast(Lorg/spockframework/mock/IMockController;)Lorg/spockframework/mock/runtime/MockController; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    INVOKEVIRTUAL org/spockframework/mock/runtime/MockController.leaveScope ()V
    ACONST_NULL
    POP
   L20
    GOTO L9
   L9
   FRAME SAME
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    INVOKEDYNAMIC cast(Lorg/spockframework/lang/ISpecificationContext;)Lorg/spockframework/runtime/SpecificationContext; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    INVOKESTATIC org/spockframework/runtime/SpockRuntime.clearCurrentBlock (Lorg/spockframework/runtime/SpecificationContext;)V
    ACONST_NULL
    POP
    GOTO L21
   L10
   FRAME FULL [apackage/TestSpec] [java/lang/Throwable]
    ASTORE 7
    ALOAD 0
    INVOKEVIRTUAL org/spockframework/lang/SpecInternals.getSpecificationContext ()Lorg/spockframework/lang/ISpecificationContext;
    INVOKEDYNAMIC cast(Lorg/spockframework/lang/ISpecificationContext;)Lorg/spockframework/runtime/SpecificationContext; [
      // handle kind 0x6 : INVOKESTATIC
      org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;
      // arguments:
      "()", 
      0
    ]
    INVOKESTATIC org/spockframework/runtime/SpockRuntime.clearCurrentBlock (Lorg/spockframework/runtime/SpecificationContext;)V
    ACONST_NULL
    POP
    ALOAD 7
    ATHROW
   L21
    LINENUMBER 9 L21
   FRAME APPEND [org/spockframework/runtime/ErrorCollector org/spockframework/runtime/ValueRecorder]
    RETURN
    LOCALVARIABLE this Lapackage/TestSpec; L8 L21 0
    LOCALVARIABLE $spock_errorCollector Lorg/spockframework/runtime/ErrorCollector; L11 L20 1
    LOCALVARIABLE $spock_valueRecorder Lorg/spockframework/runtime/ValueRecorder; L12 L20 2
    LOCALVARIABLE $spock_condition_throwable Ljava/lang/Throwable; L14 L3 3
    LOCALVARIABLE $spock_ex Ljava/lang/Throwable; L17 L7 5
    MAXSTACK = 9
    MAXLOCALS = 8
}