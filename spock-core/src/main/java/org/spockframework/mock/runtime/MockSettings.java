package org.spockframework.mock.runtime;
public class MockSettings{
    private java.lang.String name;

    public java.lang.String getName(){
        return name;
    }

    public void setName(java.lang.String name){
        this.name=name;
    }

    private java.lang.reflect.Type type;

    public java.lang.reflect.Type getType(){
        return type;
    }

    public void setType(java.lang.reflect.Type type){
        this.type=type;
    }

    private java.lang.Object instance;

    public java.lang.Object getInstance(){
        return instance;
    }

    public void setInstance(java.lang.Object instance){
        this.instance=instance;
    }

    private org.spockframework.mock.IDefaultResponse defaultResponse;

    public org.spockframework.mock.IDefaultResponse getDefaultResponse(){
        return defaultResponse;
    }

    public void setDefaultResponse(org.spockframework.mock.IDefaultResponse defaultResponse){
        this.defaultResponse=defaultResponse;
    }

    private boolean global;

    public boolean getGlobal(){
        return global;
    }

    public void setGlobal(boolean global){
        this.global=global;
    }

    private boolean verified;

    public boolean getVerified(){
        return verified;
    }

    public void setVerified(boolean verified){
        this.verified=verified;
    }

    public MockSettings(java.lang.String name,java.lang.reflect.Type type,java.lang.Object instance,org.spockframework.mock.IDefaultResponse defaultResponse,boolean global,boolean verified){
        this.name=name;
        this.type=type;
        this.instance=instance;
        this.defaultResponse=defaultResponse;
        this.global=global;
        this.verified=verified;
    }
}

