import {ref} from 'vue'
import {type DictDataEntity, getDicts} from '@/api/system/dict/data'

// Global cache to store dictionary data
// Map<dictType, DictDataEntity[]>
const dictCache = new Map<string, DictDataEntity[]>()

// Global map to store ongoing promises to prevent duplicate requests
// Map<dictType, Promise<DictDataEntity[]>>
const dictPromises = new Map<string, Promise<DictDataEntity[]>>()

/**
 * Hook to fetch and cache dictionary data
 * @param dictType Dictionary type string
 */
export function useDict(dictType: string) {
  const dictData = ref<DictDataEntity[]>([])
  const loading = ref(false)

  const loadDict = async () => {
    if (!dictType) return

    // 1. Check cache
    if (dictCache.has(dictType)) {
      dictData.value = dictCache.get(dictType)!
      return
    }

    // 2. Check if already loading
    if (dictPromises.has(dictType)) {
      loading.value = true
      try {
        const res = await dictPromises.get(dictType)!
        dictData.value = res
      } catch (e) {
        console.error(`Error waiting for shared dict request: ${dictType}`, e)
      } finally {
        loading.value = false
      }
      return
    }

    // 3. Fetch from API
    loading.value = true
    const promise = getDicts(dictType)
    dictPromises.set(dictType, promise)

    try {
      const res = await promise
      dictCache.set(dictType, res)
      dictData.value = res
    } catch (err) {
      console.error(`Failed to fetch dict: ${dictType}`, err)
      // Remove the failed promise so we can retry later
      dictPromises.delete(dictType)
    } finally {
      loading.value = false
      // We keep the promise in the map if it succeeded?
      // Actually, since we have the cache, the promise map is only needed for "in-flight".
      // But keeping it doesn't hurt, though we rely on dictCache for the data.
      // Cleaning up the promise map is fine if we rely on dictCache.
      // Ideally we clear it to avoid memory leaks if we have thousands of types,
      // but typically dict types are finite.
      // Let's clear the promise from the map upon completion to keep things clean,
      // handled by the fact that next time we hit cache.
      dictPromises.delete(dictType)
    }
  }

  // Trigger load immediately
  loadDict()

  return {
    dictData,
    loading,
    refresh: loadDict
  }
}
