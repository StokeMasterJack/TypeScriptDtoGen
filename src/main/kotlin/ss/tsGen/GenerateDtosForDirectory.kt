package ss.tsGen

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.kotlin.idea.util.isKotlinFileType
import org.jetbrains.kotlin.psi.KtClass

class GenerateDtosForDirectory : AnAction() {

    override fun update(event: AnActionEvent) {
        val currentProject = event.project
        event.presentation.isEnabledAndVisible = currentProject != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        processDir(e)
    }

    private fun getUserKtFile(e: AnActionEvent): VirtualFile? {
        return e.getData(CommonDataKeys.VIRTUAL_FILE)
    }

    private fun processDir(e: AnActionEvent) {
        val vDir = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (vDir?.isDirectory == true) {
            vDir.children.filter { it.isKotlinFileType() }.forEach {
                val psiFile = it.toPsiFile(e.project!!)
                psiFile!!.children.forEach { psiElement ->
                    if (psiElement is KtClass) {
                        val className = psiElement.name
//                        println("  Generating TypeScript interface: [${className}]")
                        val ss = generateTypeScriptDtoFromKtClass(psiElement)
                        val file = File(typeScriptDtoDir, "${className}.ts")
                        file.writeText(ss, Charsets.UTF_8)
                    }
                }
            }
        }
    }

}