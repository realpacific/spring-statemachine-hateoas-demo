package com.prashantbarahi.hateoasdemo.statemachine

data class Node<S : Enum<S>, E : Enum<E>>(val state: S) {
  val edges = mutableMapOf<E, Node<S, E>>()
}