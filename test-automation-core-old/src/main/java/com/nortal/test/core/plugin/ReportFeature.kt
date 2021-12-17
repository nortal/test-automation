package com.nortal.test.core.plugin

import io.cucumber.plugin.event.*
import java.util.function.BinaryOperator
import java.lang.IllegalStateException
import java.util.*

/**
 * This class represents a report in json format for a single feature.
 * It holds state in a tricky way, as currentStepOrHook and lastAddedStep fields keep being reassigned to different maps throughout the test suite run.
 * Such reassignment is needed since embed and add text events do not carry the information about which step they should be added to,
 * thus forcing us to keep track on which the current step is.
 *
 *
 * Other than that cucumber guarantees that events will always come in a specific order that is:
 * <pre>
 * 1. Test case started
 * 2. Step started
 * 3. Step finished
 * 4. repeat of 2 and 3 until all steps are done
 * 5. Test case finished
</pre> *
 *
 */
class ReportFeature(private val featureMap: Map<String, Any>) {
    private val elements: MutableList<MutableMap<String, Any>>?
    private var lastAddedStep: MutableMap<String, Any>? = null
    private var currentStepOrHook: MutableMap<String, Any>? = null
    private val beforeStepHooks: MutableList<Map<String, Any>> = ArrayList()

    init {
        elements = featureMap["elements"] as MutableList<MutableMap<String, Any>>?
    }

    val map: Map<String, Any>
        get() = Collections.unmodifiableMap(featureMap)

    fun addElement(entry: MutableMap<String, Any>) {
        elements!!.add(entry)
    }

    fun addStep(entry: MutableMap<String, Any>, type: StepType) {
        if (!beforeStepHooks.isEmpty()) {
            (entry.computeIfAbsent("before") { key: String? -> ArrayList<Any>() } as MutableList<Map<String, Any>>).addAll(beforeStepHooks)
            beforeStepHooks.clear()
        }
        (getLastElementOfType(type)["steps"] as MutableList<Map<String, Any>?>?)!!.add(entry)
        currentStepOrHook = entry
    }

    private fun getLastElementOfType(type: StepType): MutableMap<String, Any> {
        return elements!!.stream()
            .filter { it: Map<String, Any> -> it["type"] == type.name }
            .reduce(BinaryOperator<MutableMap<String, Any>> { a: Map<String, Any>?, b: Map<String, Any> -> b })
            .orElseThrow { IllegalStateException("Could not find $type") }
    }

    fun finishStep(testStep: TestStep?, matchMap: Map<String?, Any?>, resultMap: Map<String?, Any?>) {
        currentStepOrHook!!["match"] = matchMap
        currentStepOrHook!!["result"] = resultMap
        if (testStep is HookTestStep) {
            if (testStep.hookType == HookType.AFTER_STEP) {
                (lastAddedStep!!.computeIfAbsent("after") { key: String? -> ArrayList<Any>() } as MutableList<Map<String, Any>?>).add(
                    currentStepOrHook
                )
            }
        } else {
            lastAddedStep = currentStepOrHook
        }
        currentStepOrHook = null
    }

    fun addAfterStepHook(entry: MutableMap<String, Any>?) {
        currentStepOrHook = entry
    }

    fun addBeforeStepHook(entry: MutableMap<String, Any>) {
        currentStepOrHook = entry
        beforeStepHooks.add(entry)
    }

    fun addBeforeHook(entry: MutableMap<String, Any>?) {
        currentStepOrHook = entry
        getHookMap("before").add(entry)
    }

    fun addAfterHook(entry: MutableMap<String, Any>?) {
        currentStepOrHook = entry
        getHookMap("after").add(entry)
    }

    private fun getHookMap(hookType: String): MutableList<Map<String, Any>?> {
        return getLastElementOfType(StepType.SCENARIO).computeIfAbsent(hookType) { key: String? -> ArrayList<Any>() } as MutableList<Map<String, Any>?>
    }

    fun addText(event: WriteEvent) {
        (currentStepOrHook!!.computeIfAbsent("output") { key: String? -> ArrayList<Any>() } as MutableList<String?>).add(event.text)
    }

    fun embed(event: EmbedEvent) {
        val embedMap: MutableMap<String, Any> = HashMap()
        embedMap["mime_type"] = event.mediaType
        embedMap["data"] = Base64.getEncoder().encodeToString(event.data)
        embedMap["name"] = event.name
        (currentStepOrHook!!.computeIfAbsent("embeddings") { key: String? -> ArrayList<Any>() } as MutableList<Map<String, Any>?>).add(embedMap)
    }

    enum class StepType(override val name: String) {
        BACKGROUND("background"), SCENARIO("scenario");

    }
}