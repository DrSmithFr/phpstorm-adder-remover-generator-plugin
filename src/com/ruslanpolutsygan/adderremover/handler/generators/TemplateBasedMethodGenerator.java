package com.ruslanpolutsygan.adderremover.handler.generators;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpCodeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocParamTag;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.ruslanpolutsygan.adderremover.Util;
import com.ruslanpolutsygan.adderremover.handler.PhpMethodData;

import java.io.IOException;
import java.util.*;

public abstract class TemplateBasedMethodGenerator extends MethodGenerator {
    protected abstract String getMethodName(Field field);
    protected abstract String getMethodTemplateName();

    @Override
    public PhpMethodData[] generate(Field field) {
        String templateName = this.getMethodTemplateName();
        ArrayList<PhpMethodData> methodsData = new ArrayList<>();
        PhpClass containingClass = field.getContainingClass();
        if(containingClass != null) {
            Properties attributes = this.getMethodAttributes(field);
            FileTemplate template = FileTemplateManager.getInstance(field.getProject()).getJ2eeTemplate(templateName);
            String methodTemplate;
            try {
                methodTemplate = template.getText(attributes);
            } catch (IOException e) {
                methodTemplate = template.getText();
            }
            PhpClass dummyClass = PhpCodeUtil.createClassFromMethodTemplate(containingClass, containingClass.getProject(), methodTemplate);
            if(dummyClass != null) {
                PhpDocComment currDocComment = null;

                for(PsiElement child = dummyClass.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if(child instanceof PhpDocComment) {
                        currDocComment = (PhpDocComment)child;
                    } else if(child instanceof Method) {
                        methodsData.add(new PhpMethodData((Method)child, currDocComment));
                        currDocComment = null;
                    }
                }
            }
        }

        return methodsData.toArray(new PhpMethodData[methodsData.size()]);
    }

    private Properties getMethodAttributes(Field field) {
        Properties attributes = new Properties();

        attributes.setProperty("TYPE_HINT", this.getTypeHint(field, false));
        attributes.setProperty("TYPE_HINT_FQCN", this.getTypeHint(field, true));
        attributes.setProperty("METHOD_NAME", this.getMethodName(field));
        attributes.setProperty("TYPE_HINTED_PARAM", this.getMethodArgument(field));
        attributes.setProperty("IS_DOCTRINE_COLLECTION", this.isDoctrineCollectionField(field)?"doctrine":"");
        attributes.setProperty("FIELD_NAME", field.getName());
        attributes.setProperty("PARAM_NAME", Util.createParamName(field.getName()));
        attributes.setProperty("THIS_CLASS_NAME", field.getContainingClass().getName());

        return attributes;
    }

    private String getTypeHint(Field field, boolean isFQCN) {
        PhpDocComment phpDoc = field.getDocComment();
        if(phpDoc == null) {
            return "mixed";
        }

        PhpDocParamTag varTag = phpDoc.getVarTag();
        if(varTag == null) {
            return "mixed";
        }

        Set<String> types = varTag.getType().getTypes();
        for (String type : types) {
            if (type.endsWith("[]")) {
                int beginIndex = 0;
                if(!isFQCN) {
                    beginIndex = type.lastIndexOf('\\') + 1;
                }

                return type.substring(beginIndex, type.length() - 2);
            }
        }

        return "mixed";
    }

    private String getMethodArgument(Field field) {
        PhpDocComment phpDoc = field.getDocComment();
        String paramName = Util.createParamName(field.getName());
        if(phpDoc == null) {
            return paramName;
        }

        PhpDocParamTag varTag = phpDoc.getVarTag();
        if(varTag == null) {
            return paramName;
        }

        Set<String> types = varTag.getType().getTypes();
        for (String type : types) {
            if (type.endsWith("[]")) {
                return type.substring(type.lastIndexOf('\\') + 1, type.length() - 2) + " " + paramName;
            }
        }

        return paramName;
    }

    private boolean isDoctrineCollectionField(Field field) {
        PhpDocComment phpDoc = field.getDocComment();
        if(phpDoc == null) {
            return false;
        }

        PhpDocParamTag varTag = phpDoc.getVarTag();
        if(varTag == null) {
            return false;
        }

        Set<String> types = varTag.getType().getTypes();
        Set<String> doctrineClassNames = this.getDoctrineCollectionClassNames(field.getProject());

        types.retainAll(doctrineClassNames);

        return types.size()>0;
    }

    private Set<String> getDoctrineCollectionClassNames(Project project) {
        String doctrineCollectionInterfaceFQN = "\\Doctrine\\Common\\Collections\\Collection";

        PhpIndex index = PhpIndex.getInstance(project);
        Collection<PhpClass> doctrineCollectionClasses = index.getAllSubclasses(doctrineCollectionInterfaceFQN);

        Set<String> classNames = new HashSet<>();

        for(PhpClass c: doctrineCollectionClasses) {
            classNames.add("\\" + c.getPresentableFQN());
        }
        classNames.add(doctrineCollectionInterfaceFQN);

        return classNames;
    }
}
