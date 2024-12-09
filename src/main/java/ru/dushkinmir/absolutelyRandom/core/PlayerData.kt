package ru.dushkinmir.absolutelyRandom.core

import java.util.concurrent.ConcurrentHashMap

class PlayerData : MutableMap<String, Any> by ConcurrentHashMap()