package org.alfresco.repo.bulkimport.annotations;

import java.lang.annotation.ElementType;

@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.TYPE)
public @interface NodeType {
  String name() default "";

  String namespace() default "";

  String[] aspects() default {};
}
