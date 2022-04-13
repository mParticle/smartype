package com.mparticle.smartype.generator

import com.mparticle.smartype.api.Message
import com.mparticle.smartype.api.Serializable
import com.mparticle.smartype.api.SmartypeApiBase
import com.squareup.kotlinpoet.*
import java.io.File
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.collections.MutableList
import kotlinx.serialization.json.*

class SmartypeObject(options: GeneratorOptions) {
    companion object {
        /**
         * Smartype will use this to name [Message] objects, each [AnalyticsSchemaAdapter]
         * should fill this in based on what it knows about the schema.
         */
        const val SMARTYPE_OBJECT_NAME = "smartype_object_name"
    }
    private var dpClass: TypeSpec.Builder
    private var file: FileSpec.Builder
    private var libraryName: String = "SmartypeApi"
    private var isWeb: Boolean
    private var dedupEnums: Boolean

    init {
        isWeb = options.webOptions.enabled
        dedupEnums = options.dedupEnums
        dpClass = TypeSpec.classBuilder(libraryName)
            .addModifiers(KModifier.PUBLIC)
            .superclass(SmartypeApiBase::class)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .build())
        if (isWeb) {
            dpClass.addAnnotation(AnnotationSpec.builder(ClassName("kotlin.js", "JsExport")).build())
        }

        file = FileSpec.builder(packageName(isWeb), libraryName)
            .addComment("CODE GENERATED BY SMARTYPE, DO NOT MODIFY.")
        file.addImport("com.mparticle.smartype.api", "Message")
        file.addImport("kotlinx.serialization.json", "JsonElement")
        file.addImport("kotlinx.serialization.json", "JsonObject")
        file.addImport("kotlinx.serialization.json", "JsonPrimitive")
    }

    fun packageName(isWeb: Boolean): String {
        return if (isWeb) {
            ""
        } else {
            "com.mparticle.smartype"
        }
    }

    fun finalize(outputDirectory: String) {
        file.addType(dpClass.build())
        file.build().writeTo(File(outputDirectory))
    }

    fun configureApi(
        schema: AnalyticsSchema
    ) {
        println("Creating API with ${schema.smartypeMessageSchemas.size} message schema(s).")
        dpClass.addProperties(schema.smartypeApiPublicProperties)
        dpClass.primaryConstructor(
            FunSpec.constructorBuilder()
                .build()
        )

        val classPoints = mutableListOf<TypeSpec>()
        var emittedEnumNames = mutableListOf<String>()
        var emittedClassNames = mutableListOf<String>()
        for (messageSchema in schema.smartypeMessageSchemas) {
            val name = getObjectName(messageSchema) ?: continue
            val sanitizedName = StringHelpers.sanitize(name) ?: continue
            val classPoint = generateType(sanitizedName, messageSchema, true, emittedEnumNames, emittedClassNames)
            classPoints.add(classPoint)
        }

        addClassFunctions(dpClass, classPoints)
    }

    private fun addClassFunctions(dpClass: TypeSpec.Builder, classPoints: Collection<TypeSpec>) {
        classPoints.forEach { point ->
            var builder = FunSpec.builder(StringHelpers.lowerFirst(point.name!!)!!)
                .addModifiers(KModifier.PUBLIC)
                .returns(ClassName(packageName(isWeb), point.name!!))

            if (point.primaryConstructor != null &&
                !point.primaryConstructor?.parameters.isNullOrEmpty()
            ) {
                builder.addParameters(point.primaryConstructor?.parameters!!)
                val parameterNameList = point.primaryConstructor?.parameters!!.map { it.name }.toTypedArray()
                val parameterList = point.primaryConstructor?.parameters!!.map { "%L" }
                    .joinToString(",")
                builder.addCode("    return %L(${parameterList})", point.name!!, *parameterNameList)
            } else {
                builder.addCode("    return %L()", point.name!!)
            }

            dpClass.addFunction(
                builder.build()
            )
        }
    }

    private fun getObjectName(schema: JsonObject): String? {
        val name = schema[SMARTYPE_OBJECT_NAME]?.jsonPrimitive?.content
        if (name != null) {
            println("Found name: $name")
        } else {
            println("Unable to find name for schema: $schema")
        }

        return name
    }

    private fun packageClass(name: String) =
        ClassName(packageName(isWeb), name)

    private fun generateType(
        classNamePoint: String,
        definition: JsonObject,
        isLoggable: Boolean,
        emittedEnumNames: MutableList<String>,
        emittedClassNames: MutableList<String>
    ): TypeSpec {
        var className = classNamePoint
        if (emittedClassNames.contains(className)) {
            className = StringHelpers.dedupName(emittedClassNames, className)
        }
        emittedClassNames.add(className)

        val dpClassPoint = TypeSpec.classBuilder(className)
        dpClassPoint.addModifiers(KModifier.PUBLIC)
        if (isWeb) {
            dpClassPoint.addAnnotation(
                AnnotationSpec.builder(
                    ClassName("kotlin.js", "JsExport")
                )
                    .build()
            )
        }

        val fnBuilderJson = FunSpec.builder("toJson")
            .returns(String::class)
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("var result = \"{\"")

        val ctor = FunSpec.constructorBuilder()
        if (isLoggable) {
            dpClassPoint.superclass(Message::class)
        } else {
            dpClassPoint.addSuperinterface(Serializable::class)
        }

        val schemaDescription: JsonPrimitive?
        if (definition.containsKey("description")) {
            schemaDescription = definition["description"] as JsonPrimitive
            if (!schemaDescription.content.isBlank()) {
                dpClassPoint.addKdoc(schemaDescription.content)
            }
        }

        dpClassPoint.primaryConstructor(
            FunSpec.constructorBuilder()
                .build()
        )

        val required = mutableListOf<JsonElement>()
        if (definition.contains("required")) {
            required.addAll(definition["required"]!!.jsonArray.toMutableList())
        }
        if (definition.contains("properties")) {
            val properties = definition["properties"]?.jsonObject!!
            for (name in properties.keys) {
                val info = properties[name] as JsonObject
                val sanitizedName = StringHelpers.sanitize(name) ?: continue
                val sanitizedLower = StringHelpers.lowerFirst(sanitizedName) ?: continue

                var isRequired = requiredCheck(required, name)
                var valueConst: JsonElement? = null
                if (info.contains("const")) {
                    valueConst = info["const"]
                    isRequired = true
                }

                var type: String = "string"
                if (info.containsKey("type")) {
                    if (info["type"] is JsonPrimitive){
                        var typeJson: JsonPrimitive?
                        typeJson = info["type"] as JsonPrimitive
                        type = typeJson.content
                    } else if (info["type"] is JsonArray) {
                        var typeArray = info["type"] as JsonArray
                        if (typeArray.count() >= 1) {
                            var typeJson: JsonPrimitive?
                            typeJson = typeArray.get(0) as JsonPrimitive
                            type = typeJson.content
                            if (typeArray.count() >= 2) {
                                var typeJson2 = typeArray.get(1) as JsonPrimitive
                                if (typeJson2.content == "null") {
                                    isRequired = false
                                }
                            }
                        }
                    }
                }

                var description: String? = null
                if (info.containsKey("description")) {
                    val descriptionJson = info["description"] as JsonPrimitive
                    description = descriptionJson.content
                }

                if (!isRequired) {
                    fnBuilderJson.addCode("""
                            if (this.%L != null) {
                                
                        """.trimIndent(), sanitizedLower)
                }

                if ("string" == type) {
                    val enum = info["enum"] as? JsonArray
                    if (enum != null) {

                        var shouldAddToFile = true
                        var enumName = "$className$sanitizedName"
                        if (dedupEnums) {
                            enumName = sanitizedName

                            if (emittedEnumNames.contains(enumName)) {
                                shouldAddToFile = false
                            } else {
                                emittedEnumNames.add(enumName)
                            }
                        }

                        if (isWeb) {
                            fnBuilderJson.addStatement("""
                                result += "\"%L\":\"" + this.%L.toJson() + "\","
                            """.trimIndent(), name, sanitizedLower)
                        } else {
                            for (value in enum) {
                                fnBuilderJson.addCode("""
                                    if (this.%L == %L.%L) {
                                        result += "\"%L\":\"%L\","
                                    }

                                 """.trimIndent(),
                                 sanitizedLower,
                                 enumName,
                                 StringHelpers.sanitize(value.jsonPrimitive.content, includeUnderscores = true, allUppercaseString = true),
                                 name,
                                 StringHelpers.escapeSlashes(value.jsonPrimitive.content))
                            }

                        }

                        if (shouldAddToFile) {
                            addEnum(enumName, enum, file)
                        }

                        val typeName: TypeName = packageClass(enumName)
                        addProperty(isRequired, typeName, sanitizedLower, dpClassPoint, valueConst, description, ctor)
                    } else {
                        fnBuilderJson.addStatement("""
                            result += "\"%L\":\"" + this.%L + "\","
                        """.trimIndent(), name, sanitizedLower)
                        val typeName: TypeName = String::class.asTypeName()
                        addProperty(isRequired, typeName, sanitizedLower, dpClassPoint, valueConst, description, ctor)
                    }
                } else if ("number" == type) {
                    fnBuilderJson.addStatement("""
                            result += "\"%L\":" + this.%L + ","
                        """.trimIndent(), name, sanitizedLower)
                    val typeName: TypeName = Double::class.asTypeName()
                    addProperty(isRequired, typeName, sanitizedLower, dpClassPoint, valueConst, description, ctor)
                } else if ("boolean" == type) {
                    fnBuilderJson.addStatement("""
                            result += "\"%L\":" + this.%L + ","
                        """.trimIndent(), name, sanitizedLower)
                    val typeName: TypeName = Boolean::class.asTypeName()
                    addProperty(isRequired, typeName, sanitizedLower, dpClassPoint, valueConst, description, ctor)
                } else if ("object" == type) {

                    fnBuilderJson.addStatement("""
                        result += "\"%L\":" + this.%L.toJson() + ","
                    """.trimIndent(), name, sanitizedLower)

                    var classNameObject = "$className$sanitizedName"
                    if (emittedClassNames.contains(classNameObject)) {
                        classNameObject = StringHelpers.dedupName(emittedClassNames, classNameObject)
                    }
                    val typeName = packageClass(classNameObject)
                    addProperty(isRequired, typeName, sanitizedLower, dpClassPoint, null, description, ctor)
                    generateType(
                        classNameObject,
                        info,
                        false,
                        emittedEnumNames,
                        emittedClassNames
                    )
                } else if ("array" == type) {
                    fnBuilderJson.addStatement("""
                            result += "\"%L\":"
                            result += "JsonArray(this.%L).toJson() + "\","
                            result += ","
                        """.trimIndent(), name, sanitizedLower)
                    val typeName = MutableList::class.asClassName().parameterizedBy(String::class.asClassName())
                    addProperty(isRequired, typeName, sanitizedLower, dpClassPoint, null, description, ctor)
                }

                if (!isRequired) {
                    fnBuilderJson.addCode("""
                            }
                            
                        """.trimIndent())
                }
            }
        }
        if (!ctor.parameters.isNullOrEmpty()) {
            dpClassPoint.addModifiers(KModifier.DATA)
            dpClassPoint.primaryConstructor(ctor.build())
        }


        fnBuilderJson.addCode("""
                result = result.dropLast(1)
                result += "}"
                return result
        """.trimIndent())
        dpClassPoint.addFunction(fnBuilderJson.build())

        val classPoint = dpClassPoint.build()
        file.addType(classPoint)

        return classPoint
    }

    private fun addEnum(
        enumName: String,
        enum: JsonArray,
        file: FileSpec.Builder
    ) {
        var sanitizedEnumName = StringHelpers.sanitize(enumName, includeUnderscores = true)
        if (sanitizedEnumName == null) {
            return
        }
        val enumType: TypeSpec
        if (isWeb) {
            val builder = TypeSpec.classBuilder(sanitizedEnumName)
                .addModifiers(KModifier.PUBLIC)
                .addSuperinterface(Serializable::class)
                .addAnnotation(
                    AnnotationSpec.builder(
                        ClassName("kotlin.js", "JsExport")
                    )
                        .build()
                )

            val propertyBuilder = PropertySpec.builder("value", String::class.asTypeName())
                .addModifiers(KModifier.PUBLIC)
                .addModifiers(KModifier.LATEINIT)
                .addAnnotation(
                    AnnotationSpec.builder(ClassName("kotlin.js", "JsName")).addMember("\"value\"")
                        .build()
                )
                .mutable(true)
            builder.addProperty(propertyBuilder.build())
            val fnBuilderToJson = FunSpec.builder("toJson")
                .returns(String::class)
                .addModifiers(KModifier.PUBLIC)
                .addModifiers(KModifier.OVERRIDE)
            fnBuilderToJson.addStatement("return %L", "value")
            builder.addFunction(fnBuilderToJson.build())

            for (value in enum) {
                val origStr = value.jsonPrimitive.content
                var str = StringHelpers.sanitize(origStr, includeUnderscores = true, allUppercaseString = true)
                if (str != null) {
                    val fnBuilderEnum = FunSpec.builder(str)
                        .returns(ClassName(packageName(isWeb), sanitizedEnumName))
                        .addAnnotation(
                            AnnotationSpec.builder(ClassName("kotlin.js", "JsName")).addMember("\"$str\"")
                                .build()
                        )
                    fnBuilderEnum.addStatement("val enumVal = %L()", sanitizedEnumName)
                    fnBuilderEnum.addStatement("enumVal.value = %S", origStr)
                    fnBuilderEnum.addStatement("return enumVal")
                    builder.addFunction(fnBuilderEnum.build())
                }
            }
            enumType = builder.build()
        } else {
            val builder = TypeSpec.enumBuilder(enumName)
            for (value in enum) {
                val origStr = value.jsonPrimitive.content
                val str = StringHelpers.sanitize(origStr, includeUnderscores = true, allUppercaseString = true)
                if (str != null) {
                    builder.addEnumConstant(str, TypeSpec.anonymousClassBuilder().build())
                }
            }
            enumType = builder.build()
        }

        file.addType(enumType)
    }

    private fun requiredCheck(required: MutableList<JsonElement>?, name: String): Boolean {
        var isRequired = false
        if (required != null) {
            for (requiredName in required) {
                if (name == requiredName.jsonPrimitive.content) {
                    isRequired = true
                }
            }
        }
        return isRequired
    }

    private fun addProperty(
        isRequired: Boolean,
        typeName: TypeName,
        sanitizedLower: String,
        dpClassPoint: TypeSpec.Builder,
        valueConst: JsonElement?,
        description: String?,
        ctor: FunSpec.Builder
    ) {
        var mutableTypeName = typeName
        if (valueConst == null && !isRequired) {
            mutableTypeName = mutableTypeName.copy(nullable = true)
        }
        val propertyBuilder = PropertySpec.builder(sanitizedLower, mutableTypeName)
        propertyBuilder.addModifiers(KModifier.PUBLIC)
        if (valueConst == null) {
            propertyBuilder.initializer(sanitizedLower)
            ctor.addParameter(sanitizedLower, mutableTypeName)
        } else {
            val primitiveValue = valueConst.jsonPrimitive
            val isString: Boolean = primitiveValue.isString
            if (isString) {
                propertyBuilder.initializer(valueConst.toString())
            } else {
                propertyBuilder.initializer("%L", primitiveValue.double)
            }
        }

        propertyBuilder.addModifiers(KModifier.PUBLIC)

        if (description != null && !description.isBlank()) {
            propertyBuilder.addKdoc(description)
        }

        dpClassPoint.addProperty(
            propertyBuilder.build()
        )
    }
}
