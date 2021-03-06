package com.bloatit.framework.webprocessor.annotations.generator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner6;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.lang.model.util.TypeKindVisitor6;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.bloatit.framework.webprocessor.annotations.LengthConstraint;
import com.bloatit.framework.webprocessor.annotations.MaxConstraint;
import com.bloatit.framework.webprocessor.annotations.MinConstraint;
import com.bloatit.framework.webprocessor.annotations.NonOptional;
import com.bloatit.framework.webprocessor.annotations.Optional;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.PrecisionConstraint;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.SubParamContainer;
import com.bloatit.framework.webprocessor.annotations.generator.Generator.Clazz;

@SupportedAnnotationTypes("com.bloatit.framework.webprocessor.annotations.ParamContainer")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ParamContainerProcessor extends AbstractProcessor implements Processor {

    private static class ElementPair<T> {
        public final Element element;
        public final T other;

        public ElementPair(Element element, T other) {
            super();
            this.element = element;
            this.other = other;
        }
    }

    private final List<ElementPair<ComponentDescription>> components = new ArrayList<ElementPair<ComponentDescription>>();
    private final List<ElementPair<UrlDescription>> urls = new ArrayList<ElementPair<UrlDescription>>();

    private final Set<String> classAlreadyWritten = new HashSet<String>();

    boolean hasBeenProcessesed = false;

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment env) {
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Annotation processing ! ");

        for (final TypeElement typeElement : annotations) {
            for (final Element element : env.getElementsAnnotatedWith(typeElement)) {
                if (element.getAnnotation(ParamContainer.class) != null) {
                    parseAParamContainer(element);
                }
            }
        }

        for (final ElementPair<UrlDescription> entry : urls) {
            for (final ElementPair<UrlDescription> other : urls) {
                if (!other.element.equals(entry.element)) {
                    if (entry.other.getComponent()
                                   .getCodeName()
                                   .replaceAll("\\%[a-zA-Z0-9_#()\\.]+\\%", "12")
                                   .matches(other.other.getComponent().getCodeName().replaceAll("\\%[a-zA-Z0-9_#()\\.]+\\%", "[a-zA-Z0-9_%]+"))) {
                        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "2 pages matches the same input.", other.element);
                        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "2 pages matches the same input.", entry.element);
                    }
                }
            }
        }

        for (final ElementPair<ComponentDescription> entry : components) {
            createFile(new CodeGenerator().generateComponentClass(entry.other));
        }
        for (final ElementPair<UrlDescription> entry : urls) {
            createFile(new CodeGenerator().generateUrlClass(entry.other));
        }

        return true;
    }

    private void createFile(final Clazz clazz) {
        if (classAlreadyWritten.contains(clazz.getQualifiedName())) {
            return;
        }
        BufferedWriter out = null;
        try {
            // this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
            // "writing " + clazz.getQualifiedName());
            final JavaFileObject classFile = this.processingEnv.getFiler().createSourceFile(clazz.getQualifiedName());
            out = new BufferedWriter(classFile.openWriter());
            out.write(clazz.toString());
            classAlreadyWritten.add(clazz.getQualifiedName());
        } catch (final Exception e) {
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void parseAParamContainer(final Element element) {
        parseAParamContainer(element, null, null, null);
    }

    private void parseAParamContainer(final Element element, final UrlDescription urlChild) {
        parseAParamContainer(element, null, null, urlChild);
    }

    private void parseAParamContainer(final Element element, final Element fromAttribute, final ComponentDescription father) {
        parseAParamContainer(element, fromAttribute, father, null);
    }

    private void parseAParamContainer(final Element element,
                                      final Element fromAttribute,
                                      final ComponentDescription father,
                                      final UrlDescription urlChild) {
        final ParamContainer paramContainer = element.getAnnotation(ParamContainer.class);
        if (paramContainer == null) {
            return;
        }

        final ComponentDescription component;
        if (fromAttribute != null) {
            component = new ComponentDescription(element, paramContainer, fromAttribute.getSimpleName().toString());
        } else {
            component = new ComponentDescription(element, paramContainer, null);
        }
        components.add(new ElementPair<ComponentDescription>(element, component));
        if (father != null) {
            father.addSubComponent(component);
        }

        if (!paramContainer.isComponent()) {
            final UrlDescription url = new UrlDescription(component, element, isAction(element));
            urls.add(new ElementPair<UrlDescription>(element, url));
            parseSuperClass(element, url);
            if (urlChild != null) {
                urlChild.setFather(url);
            }
        }

        for (final Element enclosed : element.getEnclosedElements()) {
            parseARequestParam(component, enclosed);
            if (enclosed.getAnnotation(SubParamContainer.class) != null) {
                parseEnclosedParamContainer(component, enclosed);
            }
        }

    }

    private boolean isAction(final Element element) {
        if (element == null) {
            return false;
        }
        if (element.getSimpleName().toString().equals("Action")) {
            return true;
        }

        return element.accept(new ElementScanner6<Boolean, Object>() {

            @Override
            public Boolean visitType(final TypeElement e, final Object p) {
                return isAction(e.getSuperclass());
            }
        }, false);

    }

    private boolean isAction(final TypeMirror type) {
        if (type == null) {
            return false;
        }
        final Boolean accept = type.accept(new TypeKindVisitor6<Boolean, Object>() {

            @Override
            public Boolean visitDeclared(final DeclaredType t, final Object p) {
                return isAction(t.asElement());
            }
        }, null);

        return accept != null && accept;

    }

    private void parseSuperClass(final Element element, final UrlDescription urlDescription) {
        final SimpleTypeVisitor6<Element, ProcessingEnvironment> vs = new SimpleTypeVisitor6<Element, ProcessingEnvironment>() {
            @Override
            public Element visitDeclared(final DeclaredType t, final ProcessingEnvironment p) {
                final List<? extends TypeMirror> directSupertypes = p.getTypeUtils().directSupertypes(t);
                if (directSupertypes.size() > 0) {
                    return p.getTypeUtils().asElement(directSupertypes.get(0));
                }
                return null;
            }
        };
        final Element superElement = element.asType().accept(vs, this.processingEnv);
        if (superElement != null && superElement.getAnnotation(ParamContainer.class) != null) {
            parseAParamContainer(superElement, urlDescription);
        }
    }

    private void parseARequestParam(final ComponentDescription component, final Element attribute) {
        final RequestParam requestParam = attribute.getAnnotation(RequestParam.class);
        final Optional optional = attribute.getAnnotation(Optional.class);
        final NonOptional nonOptional = attribute.getAnnotation(NonOptional.class);
        final MinConstraint minConstraint = attribute.getAnnotation(MinConstraint.class);
        final MaxConstraint maxConstraint = attribute.getAnnotation(MaxConstraint.class);
        final LengthConstraint lengthConstraint = attribute.getAnnotation(LengthConstraint.class);
        final PrecisionConstraint precisionConstraint = attribute.getAnnotation(PrecisionConstraint.class);

        if (optional != null && nonOptional != null) {
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                                                          "Optional and non optional annotations on the same parameter.",
                                                          attribute);
        }

        if (requestParam != null) {
            component.addParameter(new ParameterDescription(attribute,
                                                            requestParam,
                                                            optional,
                                                            nonOptional,
                                                            lengthConstraint,
                                                            maxConstraint,
                                                            minConstraint,
                                                            precisionConstraint));
        }
    }

    private void parseEnclosedParamContainer(final ComponentDescription component, final Element attribute) {
        if (attribute.getAnnotation(RequestParam.class) == null) {
            // Find if the type of the attribute has a ParamContainer annotation
            final TypeKindVisitor6<Element, Integer> vs = new TypeKindVisitor6<Element, Integer>() {
                @Override
                public Element visitDeclared(final DeclaredType t, final Integer p) {
                    return t.asElement();
                }
            };
            final Element newElement = attribute.asType().accept(vs, 0);
            if (newElement != null && newElement.getAnnotation(ParamContainer.class) != null) {
                parseAParamContainer(newElement, attribute, component);
            }
        }
    }

    // try {
    //
    // /* Creating java code model classes */
    // JCodeModel jCodeModel = new JCodeModel();
    //
    // /* Adding packages here */
    // JPackage jp = jCodeModel._package(factroyPackage);
    //
    // /* Giving Class Name to Generate */
    // JDefinedClass jc = jp._class("GeneratedFactory");
    //
    // /* Adding annotation for the Class */
    // jc.annotate(com.myannotation.AnyXYZ.class);
    //
    // /* Adding class level coment */
    // JDocComment jDocComment = jc.javadoc();
    // jDocComment.add("Class Level Java Docs");
    //
    //
    // /* Adding method to the Class which is public static and returns
    // com.somclass.AnyXYZ.class */
    // String mehtodName = "myFirstMehtod";
    // JMethod jmCreate = jc.method(JMod.PUBLIC | JMod.STATIC,
    // com.somclass.AnyXYZ.class,
    // "create" + mehtodName);
    //
    // /* Addign java doc for method */
    // jmCreate.javadoc().add("Method Level Java Docs");
    //
    // /* Adding method body */
    // JBlock jBlock = jmCreate.body();
    //
    // /* Defining method parameter */
    // JType jt = getTypeDetailsForCodeModel(jCodeModel, "Unsigned32");
    // if (jt != null) {
    // jmCreate.param(jt, "data");
    // } else {
    // jmCreate.param(java.lang.String.class, "data");
    // }
    //
    // /* Defining some class Variable in mthod body */
    // JClass jClassavpImpl = jCodeModel.ref(com.somclass.AnyXYZ.class);
    // jvarAvpImpl = jBlock.decl(jClassavpImpl, "varName");
    // jvarAvpImpl.init(JExpr._new(jClassavpImpl));
    //
    //
    // /* Adding some direct statement */
    // jBlock.directStatement("varName.setCode(100);");
    //
    // /* returning varibalbe */
    // jBlock._return(jvarAvpImpl);
    //
    // /* Building class at given location */
    // jCodeModel.build(new File("generated/src"));
    //
    // } catch (JAXBException ex) {
    // logger.log(Level.SEVERE, "JAXBException:" + ex);
    // ex.printStackTrace();
    // } catch (Exception ex) {
    // logger.log(Level.SEVERE, "Other Exception which in not catched:" + ex);
    // ex.printStackTrace();
    // }
    // }

}
