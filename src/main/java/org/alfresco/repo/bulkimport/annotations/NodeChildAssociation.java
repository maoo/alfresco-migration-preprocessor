package org.alfresco.repo.bulkimport.annotations;

import java.lang.annotation.ElementType;

@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.FIELD)
public @interface NodeChildAssociation {
  String name() default "";

  String namespace() default "";
}
