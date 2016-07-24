yieldUnescaped '<!DOCTYPE html>'
html(lang: 'en') {
  head {
    meta(chaset: "UTF-8")
    title("Source Server View")
    style {
      includeUnescaped "style.css"
    }
  }
  body() {
    section(id: 'root') {
    }

    div(id: "example")
    script(type: "text/javascript", src:"/static/bundle.js")
  }
}