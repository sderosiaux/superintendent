package com.sderosiaux.superintendent

import com.sderosiaux.superintendent.config.ConfigLoader
import zio._
import zio.console.putStrLn

object Main extends zio.App {

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      conf <- ConfigLoader.config()
      _ <- putStrLn(conf.toString)
    } yield conf).foldM(t => putStrLn(s"Failure: $t") *> ZIO.succeed(1), _ => ZIO.succeed(0))
}
