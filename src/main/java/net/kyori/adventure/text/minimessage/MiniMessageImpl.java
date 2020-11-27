/*
 * This file is part of adventure-text-minimessage, licensed under the MIT License.
 *
 * Copyright (c) 2018-2020 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.kyori.adventure.text.minimessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.markdown.MarkdownFlavor;
import net.kyori.adventure.text.minimessage.markdown.MiniMarkdownParser;
import net.kyori.adventure.text.minimessage.transformation.Transformation;
import net.kyori.adventure.text.minimessage.transformation.TransformationRegistry;
import net.kyori.adventure.text.minimessage.transformation.TransformationType;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

class MiniMessageImpl implements MiniMessage {

  static final Function<String, ComponentLike> DEFAULT_PLACEHOLDER_RESOLVER = s -> null;

  static final MiniMessage INSTANCE = new MiniMessageImpl(false, MarkdownFlavor.defaultFlavor(), new TransformationRegistry(), DEFAULT_PLACEHOLDER_RESOLVER);
  static final MiniMessage MARKDOWN = new MiniMessageImpl(true, MarkdownFlavor.defaultFlavor(), new TransformationRegistry(), DEFAULT_PLACEHOLDER_RESOLVER);

  private final boolean markdown;
  private final MarkdownFlavor markdownFlavor;
  private final MiniMessageParser parser;

  MiniMessageImpl(final boolean markdown, final @NonNull MarkdownFlavor markdownFlavor, final @NonNull TransformationRegistry registry, final Function<String, ComponentLike> placeholderResolver) {
    this.markdown = markdown;
    this.markdownFlavor = markdownFlavor;
    this.parser = new MiniMessageParser(registry, placeholderResolver);
  }

  @Override
  public @NonNull Component deserialize(@NonNull String input) {
    if(this.markdown) {
      input = MiniMarkdownParser.parse(input, this.markdownFlavor);
    }
    return this.parser.parseFormat(input);
  }

  @Override
  public @NonNull String serialize(final @NonNull Component component) {
    return MiniMessageSerializer.serialize(component);
  }

  @Override
  public @NonNull Component parse(@NonNull String input, final @NonNull String... placeholders) {
    if(this.markdown) {
      input = MiniMarkdownParser.parse(input, this.markdownFlavor);
    }
    return this.parser.parseFormat(input, placeholders);
  }

  @Override
  public @NonNull Component parse(@NonNull String input, final @NonNull Map<String, String> placeholders) {
    if(this.markdown) {
      input = MiniMarkdownParser.parse(input, this.markdownFlavor);
    }
    return this.parser.parseFormat(input, placeholders);
  }

  @Override
  public @NonNull Component parse(final @NonNull String input, final @NonNull Object... placeholders) {
    if(placeholders.length % 2 != 0) {
      throw new IllegalArgumentException("Each placeholder must have a key and value");
    }

    final Template[] templates = new Template[placeholders.length / 2];
    for(int i = 0; i < placeholders.length; i += 2) {
      if(!(placeholders[i] instanceof String)) {
        throw new IllegalArgumentException("Argument " + i + " in placeholders must be String: is key");
      }
      final String key = (String) placeholders[i];

      final Object rawValue = placeholders[i + 1];
      final Component value;
      if(rawValue instanceof String) {
        value = Component.text((String) rawValue);
      } else if(rawValue instanceof ComponentLike) {
        value = ((ComponentLike) rawValue).asComponent();
      } else {
        throw new IllegalArgumentException("Argument " + (i + 1) + " in placeholders must be Component or String: is value");
      }
      templates[i / 2] = Template.of(key, value);
    }

    return this.parse(input, templates);
  }

  @Override
  public @NonNull Component parse(@NonNull String input, final @NonNull Template... placeholders) {
    if(this.markdown) {
      input = MiniMarkdownParser.parse(input, this.markdownFlavor);
    }
    return this.parser.parseFormat(input, placeholders);
  }

  @Override
  public @NonNull Component parse(@NonNull String input, final @NonNull List<Template> placeholders) {
    if(this.markdown) {
      input = MiniMarkdownParser.parse(input, this.markdownFlavor);
    }
    return this.parser.parseFormat(input, placeholders);
  }

  @Override
  public @NonNull String escapeTokens(final @NonNull String input) {
    return this.parser.escapeTokens(input);
  }

  @Override
  public @NonNull String stripTokens(@NonNull String input) {
    if(this.markdown) {
      input = MiniMarkdownParser.stripMarkdown(input, this.markdownFlavor);
    }
    return this.parser.stripTokens(input);
  }

  @Override
  public @NonNull Builder toBuilder() {
    return new BuilderImpl(this);
  }

  /* package */ static final class BuilderImpl implements Builder {
    private boolean markdown = false;
    private MarkdownFlavor markdownFlavor = MarkdownFlavor.defaultFlavor();
    private final TransformationRegistry registry = new TransformationRegistry();
    private Function<String, ComponentLike> placeholderResolver = DEFAULT_PLACEHOLDER_RESOLVER;

    BuilderImpl() {
    }

    BuilderImpl(final MiniMessageImpl serializer) {
      this.markdown = serializer.markdown;
    }

    @Override
    public @NonNull Builder markdown() {
      this.markdown = true;
      return this;
    }

    @Override
    public @NonNull Builder removeDefaultTransformations() {
      this.registry.clear();
      return this;
    }

    @Override
    public @NonNull Builder transformation(final TransformationType<? extends Transformation> type) {
      this.registry.register(type);
      return this;
    }

    @SafeVarargs
    @Override
    public final @NonNull Builder transformations(final TransformationType<? extends Transformation>... types) {
      for(final TransformationType<? extends Transformation> type : types) {
        this.registry.register(type);
      }
      return this;
    }

    @Override
    public @NonNull Builder markdownFlavor(final MarkdownFlavor markdownFlavor) {
      this.markdownFlavor = markdownFlavor;
      return this;
    }

    @Override
    public @NonNull Builder placeholderResolver(final Function<String, ComponentLike> placeholderResolver) {
      this.placeholderResolver = placeholderResolver;
      return this;
    }

    @Override
    public @NonNull MiniMessage build() {
      if(this.markdown) {
        return new MiniMessageImpl(true, this.markdownFlavor, this.registry, this.placeholderResolver);
      } else {
        return new MiniMessageImpl(false, MarkdownFlavor.defaultFlavor(), this.registry, this.placeholderResolver);
      }
    }
  }
}
