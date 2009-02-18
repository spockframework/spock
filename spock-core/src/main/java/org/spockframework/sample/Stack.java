package org.spockframework.sample;

import java.util.ArrayList;

public class Stack<E> {

 private final ArrayList<E> list;

 public Stack() {
  this.list = new ArrayList<E>();
 }

 public void push(E value) {
  if(value == null){
   throw new RuntimeException("Can't push null");
  }else{
   this.list.add(value);
  }
 }

 public E pop() {
  if(this.list.size() > 0){
   return this.list.remove(this.list.size()-1);
  }else{
   throw new RuntimeException("Nothing to pop");
  }
 }

 public E peek() {
  if(this.list.size() > 0){
   return this.list.get(this.list.size()-1);
  }else{
   return null;
  }
 }
}