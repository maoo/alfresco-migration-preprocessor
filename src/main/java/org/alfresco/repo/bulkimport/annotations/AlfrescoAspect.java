package org.alfresco.repo.bulkimport.annotations;

@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(java.lang.annotation.ElementType.METHOD)
public @interface AlfrescoAspect {
  String name() default "";

  String namespace() default "";
}
