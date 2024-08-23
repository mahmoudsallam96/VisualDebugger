# TPS Route Visual Debugger (IntelliJ Plugin)
Try it out:

```./gradlew run```

This should spawn a new IntelliJ instance. Try debugging from there.


Remove gradle caches:
see where space is wasted:

```cd /Users/mahmoudsallam/.gradle/caches/```

and

```du -sh * | sort -rh | head -n 20```

then

```cd /Users/mahmoudsallam/.gradle/caches/```

finally:

```rm -rf <whatever-is-taking-the-most-space>```

