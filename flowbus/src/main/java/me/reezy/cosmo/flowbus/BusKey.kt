package me.reezy.cosmo.flowbus

class BusKey<T>(val name: String, val clazz: Class<T>) {

    override fun equals(other: Any?): Boolean {
        return other is BusKey<*> && clazz == other.clazz && name == other.name
    }

    override fun hashCode(): Int {
        return 31 * name.hashCode() + clazz.hashCode()
    }
}