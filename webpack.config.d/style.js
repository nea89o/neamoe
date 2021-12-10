config.resolve.modules.push("src/main/resources/")
config.module.rules.push({test: /\.txt$/, type: 'asset/source'})
config.module.rules.push({test: /\.(png|jpg|jpeg|svg|gif)$/i, type: 'asset/resource'})