package org.testcontainers.containers

import java.util.concurrent.Future

class KGenericContainer(image: Future<String>) : GenericContainer<KGenericContainer>(image)