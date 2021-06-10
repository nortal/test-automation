package com.nortal.test.core.services.hooks;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class HookContext {
	boolean goldenDataLockIsMandatory;
}
