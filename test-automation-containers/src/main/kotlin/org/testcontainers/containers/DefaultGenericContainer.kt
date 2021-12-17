package org.testcontainers.containers

/**
 * Alternative to GenericContainer which hides overused generics.
 */
class DefaultGenericContainer(dockerImageName: String) : GenericContainer<DefaultGenericContainer>(dockerImageName)