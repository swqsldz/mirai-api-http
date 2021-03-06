package net.mamoe.mirai.api.http.service.heartbeat

import io.ktor.util.InternalAPI
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.api.http.service.MiraiApiHttpService
import net.mamoe.mirai.api.http.util.HttpClient
import net.mamoe.mirai.console.plugins.PluginBase
import java.util.*
import kotlin.concurrent.timerTask

/**
 * 心跳服务
 */
class HeartBeatService(override val console: PluginBase) : MiraiApiHttpService {

    val config = HeartBeatConfig(console.loadConfig("setting.yml"))

    /**
     * 心跳计时器
     */
    private var timer: Timer = Timer("HeartBeat", false)

    override fun onLoad() {
    }

    override fun onEnable() {
        timer.schedule(timerTask {
            if (config.enable) {
                console.launch {
                    pingAllDestinations()
                }
            }
        }, config.delay, config.period)

        console.logger.info("心跳模块启用状态: ${config.enable}")
    }

    override fun onDisable() {
        timer.cancel()
        timer.purge()

        console.logger.info("心跳模块已禁用")
    }

    /**
     * 发送心跳到所有目标地址
     */
    private suspend fun pingAllDestinations() {
        config.destinations.forEach {
            ping(it)
        }
    }

    /**
     * 发送心跳到指定地址
     */
    private suspend fun ping(destination: String) {
        try {
            HttpClient.post(destination, config.extraBody, config.extraHeaders)
        } catch (e: Exception) {
            console.logger.error("发送${destination}心跳失败: ${e.message}")
        }
    }
}
