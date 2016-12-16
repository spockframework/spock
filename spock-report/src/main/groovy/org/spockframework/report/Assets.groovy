/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.report

class Assets {
  private static final String CDN = "https://cdnjs.cloudflare.com/ajax/libs"
  private final String location
  private final String mode

  Assets(boolean local, boolean debug) {
    location = local ? "local" : "remote"
    mode = debug ? "debug" : "normal"
  }

  List getCss() {
    CSS[location][mode]
  }

  List getImg() {
    IMG[location]
  }

  List getJs() {
    JS[location][mode]
  }

  private static CSS =
    [
      local : [
        normal: [
          [href: "css/bootstrap.min.css"],
          [href: "css/bootstrap-responsive.min.css"],
          [href: "css/bootstrap-multiselect.css"],
          [href: "css/colorbox.css"],
          [href: "css/report.css"]
        ],
        debug : [
          [href: "css/bootstrap.css"],
          [href: "css/bootstrap-responsive.css"],
          [href: "css/bootstrap-multiselect.css"],
          [href: "css/colorbox.css"],
          [href: "css/report.css"]
        ]
      ],
      remote: [
        normal: [
          [href: "$CDN/twitter-bootstrap/2.3.2/css/bootstrap-responsive.min.css", crossorigin: "anonymous"],
          [href: "$CDN/twitter-bootstrap/2.3.2/css/bootstrap.min.css", crossorigin: "anonymous"],
          [href: "$CDN/bootstrap-multiselect/0.9.13/css/bootstrap-multiselect.css", crossorigin: "anonymous"],
          [href: "$CDN/jquery.colorbox/1.3.27/example3/colorbox.css", crossorigin: "anonymous"],
          [href: "css/report.css"]
        ],
        debug : [
          [href: "$CDN/twitter-bootstrap/2.3.2/css/bootstrap.css", crossorigin: "anonymous"],
          [href: "$CDN/twitter-bootstrap/2.3.2/css/bootstrap-responsive.css", crossorigin: "anonymous"],
          [href: "$CDN/bootstrap-multiselect/0.9.13/css/bootstrap-multiselect.css", crossorigin: "anonymous"],
          [href: "$CDN/jquery.colorbox/1.3.27/example3/colorbox.css", crossorigin: "anonymous"],
          [href: "css/report.css"]
        ]
      ]
    ]

  private static IMG =
    [
      local : [
        "img/colorbox/border.png",
        "img/colorbox/controls.png",
        "img/colorbox/loading.gif",
        "img/colorbox/loading_background.png",
        "img/colorbox/overlay.png",
        "img/glyphicons-halflings-white.png",
        "img/glyphicons-halflings.png",
        "img/glyphicons_circle_minus_small.png",
        "img/glyphicons_circle_plus_small.png"
      ],
      remote: [
        "$CDN/jquery.colorbox/1.3.27/example3/images/border.png",
        "$CDN/jquery.colorbox/1.3.27/example3/images/controls.png",
        "$CDN/jquery.colorbox/1.3.27/example3/images/loading.gif",
        "$CDN/jquery.colorbox/1.3.27/example3/images/loading_background.png",
        "$CDN/jquery.colorbox/1.3.27/example3/images/overlay.png",
        "$CDN/twitter-bootstrap/2.3.2/img/glyphicons-halflings-white.png",
        "$CDN/twitter-bootstrap/2.3.2/img/glyphicons-halflings.png",
        "img/glyphicons_circle_minus_small.png",
        "img/glyphicons_circle_plus_small.png"
      ]
    ]

  private static JS =
    [
      local : [
        normal: [
          [src: "js/jquery.min.js"],
          [src: "js/jquery.peity.min.js"],
          [src: "js/jquery.colorbox.min.js"],
          [src: "js/bootstrap.min.js"],
          [src: "js/bootstrap-multiselect.js"],
          [src: "js/underscore.min.js"],
          [src: "js/handlebars.min.js"],
          [src: "js/report.js"]
        ],
        debug : [
          [src: "js/jquery.js"],
          [src: "js/jquery.peity.js"],
          [src: "js/jquery.colorbox.js"],
          [src: "js/bootstrap.js"],
          [src: "js/bootstrap-multiselect.js"],
          [src: "js/underscore.js"],
          [src: "js/handlebars.js"],
          [src: "js/report.js"]
        ]
      ],
      remote: [
        normal: [
          [src: "$CDN/jquery/3.1.1/jquery.min.js", crossorigin: "anonymous"],
          [src: "$CDN/peity/3.2.1/jquery.peity.min.js", crossorigin: "anonymous"],
          [src: "$CDN/jquery.colorbox/1.6.4/jquery.colorbox-min.js", crossorigin: "anonymous"],
          [src: "$CDN/twitter-bootstrap/2.3.2/js/bootstrap.min.js",crossorigin: "anonymous"],
          [src: "$CDN/bootstrap-multiselect/0.9.13/js/bootstrap-multiselect.min.js", crossorigin: "anonymous"],
          [src: "$CDN/underscore.js/1.8.3/underscore-min.js", crossorigin: "anonymous"],
          [src: "$CDN/handlebars.js/4.0.5/handlebars.min.js", crossorigin: "anonymous"],
          [src: "js/report.js"]
        ],
        debug : [
          [src: "$CDN/jquery/3.1.1/jquery.js", crossorigin: "anonymous"],
          [src: "$CDN/peity/3.2.1/jquery.peity.js", crossorigin: "anonymous"],
          [src: "$CDN/jquery.colorbox/1.6.4/jquery.colorbox.js", crossorigin: "anonymous"],
          [src: "$CDN/twitter-bootstrap/2.3.2/js/bootstrap.js", crossorigin: "anonymous"],
          [src: "$CDN/bootstrap-multiselect/0.9.13/js/bootstrap-multiselect.js", crossorigin: "anonymous"],
          [src: "$CDN/underscore.js/1.8.3/underscore.js", crossorigin: "anonymous"],
          [src: "$CDN/handlebars.js/4.0.5/handlebars.js", crossorigin: "anonymous"],
          [src: "js/report.js"]
        ]
      ]
    ]
}
