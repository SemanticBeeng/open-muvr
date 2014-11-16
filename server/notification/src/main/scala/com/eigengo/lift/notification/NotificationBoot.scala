package com.eigengo.lift.notification

import akka.actor.{ActorSystem, ActorRef}

case class NotificaitonBoot(notification: ActorRef)

object NotificaitonBoot {

  def boot(implicit system: ActorSystem): NotificaitonBoot = {
    val notification = system.actorOf(Notification.props, Notification.name)
    NotificaitonBoot(notification)
  }

}