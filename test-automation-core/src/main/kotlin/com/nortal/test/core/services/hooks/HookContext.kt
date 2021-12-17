package com.nortal.test.core.services.hooks

import lombok.Builder
import lombok.Value

@Builder
@Value
class HookContext {
    var goldenDataLockIsMandatory = false
}