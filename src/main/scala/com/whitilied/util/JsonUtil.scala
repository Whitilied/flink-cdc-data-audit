package com.whitilied.util

import com.fasterxml.jackson.core.JsonParser.Feature
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, PropertyNamingStrategy}
import com.fasterxml.jackson.module.scala.{DefaultScalaModule, ScalaObjectMapper}

import scala.reflect.{ClassTag, Manifest, classTag}

object JsonUtil {

  val mapper: ObjectMapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
  mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

  def toJson(value: Map[Symbol, Any]): String = {
    toJson(value map { case (k, v) => k.name -> v })
  }

  def toJson(value: Any): String = mapper.writeValueAsString(value)

  def toMap[V](json: String)(implicit m: Manifest[V]): Map[String, V] = {
    fromJson[Map[String, V]](json)
  }

  def fromJson[T: ClassTag](json: String): T = {
    mapper.readValue[T](json, classTag[T].runtimeClass.asInstanceOf[Class[T]])
  }
}
