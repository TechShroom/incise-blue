package com.techshroom.inciseblue.ide

class EclipseConfiguration {
    companion object {
        private const val JAVAFX_CONTAINER = "org.eclipse.fx.ide.jdt.core.JAVAFX_CONTAINER"
    }

    val extraContainers: MutableList<String> = mutableListOf()
    var addJavaFx: Boolean = false

    val allExtraContainers: List<String>
        get() {
            val containers = ArrayList(extraContainers)
            if (addJavaFx) {
                containers.add(JAVAFX_CONTAINER)
            }
            return containers
        }
}
