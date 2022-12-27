package ss.tsGen

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import java.io.File
import kotlin.reflect.KClass
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.jetbrains.kotlin.idea.base.utils.fqname.fqName
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.SimpleType


 val reposDir: File = File("/Volumes/repos")
 val typeScriptDtoDir: File = File(reposDir, "ramko/ramko-react/src/rc/src/dto")

val KtParameter.typeName: String
    get() {
        return this.type()!!.fqName!!.shortName().identifier
    }


object STypes {
    private val sPropTypes: List<TT> = listOf(
        TT("Id", "Id", "string"),
        TT("String", "Str", "string"),
        TT("Int", "Int", "number"),
        TT("Double", "Float", "number"),
        TT("Float", "Float", "number"),
        TT("Boolean", "Bool", "boolean"),
        TT("LocalDate", "LocalDate", "string"),
        TT("LocalDateTime", "LocalDateTime", "string")
    )

    private val map: Map<String, TT> by lazy { sPropTypes.associateBy { it.simpleClassName } }

    val names: Set<String> = map.keys

    val nameList = names.toList().sorted()


    fun allProps(cls: KtClass): List<KtParameter> {
        return cls.primaryConstructor!!.valueParameters
    }

    fun simpleProps(cls: KtClass): List<KtParameter> {
        return allProps(cls).filter { isSType(it.typeName) }
    }

    fun simplePropTypesTT(cls: KtClass): List<TT> {
        return simpleProps(cls).map { STypes.getTT(it.typeName) }
    }

    fun simplePropTypes(cls: KtClass): List<String> {
        return simplePropTypesTT(cls).map { it.tsDaveName }
    }

    fun simplePropTsTypes(cls: KtClass): Set<String> {
        return simplePropTypesTT(cls).map { it.tsDaveName }.toSet()
    }

    fun simpleImportStatement(cls: KtClass): String {
        val simplePropTypes = cls.primaryConstructor!!.valueParameters.asSequence().filter { isSType(it) }.map { tsSimpleType(it) }.toSet().toList().sorted().toList()
        return """import {${simplePropTypes.joinToString(separator = ", ")}} from 'util/aliases'"""
    }

    fun complexImportStatement(cls: KtClass): String {
        val complexPropTypes = cls.primaryConstructor!!.valueParameters.asSequence().filter { !isSType(it) }.map { it.typeName }.toSet().toList().sorted().toList()
        return complexPropTypes.joinToString(separator = "\n") { "import {${it}} from './${it}'" }
    }

    fun isSType(n: String): Boolean {
        return sPropTypes.map { it.simpleClassName }.toSet().contains(n)
    }

    fun isSType(kotlinType: KotlinType?): Boolean {
        val simpleType: SimpleType = requireNotNull(kotlinType) as SimpleType
        val n = simpleType.fqName!!.shortName().identifier
        return isSType(n)
    }

    fun isSType(ktParameter: KtParameter?): Boolean {
        val p: KtParameter = requireNotNull(ktParameter)
        return STypes.isSType(p.type())
    }

    fun getTT(name: String): TT = map.getValue(name)

    fun getTT(simpleType: SimpleType): TT = map.getValue(simpleType.fqName!!.shortName().identifier)
    fun getTT(ktParameter: KtParameter): TT = getTT(ktParameter.type() as SimpleType)
}

data class TT(val simpleClassName: String, val tsDaveName: String, val tsStandardName: String)

fun tsSimpleType(prop: KtParameter, useDaveNames: Boolean = true): String {
    val simpleType: SimpleType = prop.type()!! as SimpleType
    val tt = STypes.getTT(simpleType)
    val baseType = if (useDaveNames) {
        val t = tt.tsDaveName
        if (t == "Int" && prop.name == "id") "Id" else t
    } else {
        tt.tsStandardName
    }
    return if (simpleType.isMarkedNullable) {
        if (useDaveNames) "${baseType}N"
        else "$baseType | null"
    } else {
        baseType
    }
}


fun generateTypeScriptDtoFromKtClass(cls: KtClass): String {
    return buildString {
        appendLine()
        appendLine(STypes.complexImportStatement(cls))
        appendLine(STypes.simpleImportStatement(cls))
        appendLine()
        appendLine("export interface ${cls.name} {")
        val props: List<KtParameter> = cls.primaryConstructor!!.valueParameters
        props.forEach {
            val propName = it.name
            if (STypes.isSType(it)) {
                val typeName = tsSimpleType(it)
                appendLine("    ${propName}: ${typeName};")
            } else {
                appendLine("    ${propName}: ${it.typeName};")
            }
        }
        appendLine("}")
    }
}

class GenTypeScriptDto : AnAction() {

    override fun update(event: AnActionEvent) {
        val currentProject = event.project
        event.presentation.isEnabledAndVisible = currentProject != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val className = getUserKtClass(e).name
        val ss = generateTypeScriptDto(e)
        val file = File(typeScriptDtoDir, "${className}.ts")
        file.writeText(ss, Charsets.UTF_8)
    }


    private fun getUserKtClass(e: AnActionEvent): KtClass {
        return e.getData(CommonDataKeys.NAVIGATABLE)!! as KtClass
    }


    private fun generateTypeScriptDto(e: AnActionEvent): String {
        val cls: KtClass = getUserKtClass(e)
        return generateTypeScriptDtoFromKtClass(cls)
    }

}

