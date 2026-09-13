package com.paybridge

import android.app.Application

/**
 * Composition root. No DI framework (Hilt) is used here — the object graph is small enough
 * that a set of lazily-constructed singletons exposed off this class is easier to read through
 * in a portfolio review than KSP-generated Hilt components, and is a cheap swap later if this
 * ever grows past a handful of injectable classes.
 */
class PayBridgeApp : Application()
